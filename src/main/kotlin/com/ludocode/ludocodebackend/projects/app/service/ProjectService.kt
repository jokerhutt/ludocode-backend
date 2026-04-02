package com.ludocode.ludocodebackend.projects.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.languages.api.dto.Languages
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.request.RenameProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardListResponse
import com.ludocode.ludocodebackend.projects.app.mapper.ProjectCardMapper
import com.ludocode.ludocodebackend.projects.app.mapper.ProjectMapper
import com.ludocode.ludocodebackend.projects.app.util.ProjectSnapshotValidator
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import com.ludocode.ludocodebackend.projects.infra.repository.ProjectFileRepository
import com.ludocode.ludocodebackend.projects.infra.repository.UserProjectRepository
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageDeleteRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StorageGetRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*


@Service
class ProjectService(
    private val userProjectRepository: UserProjectRepository,
    private val projectFileRepository: ProjectFileRepository,
    private val projectMapper: ProjectMapper,
    private val clock: Clock,
    private val storagePortForServices: StoragePortForServices,
    private val subscriptionService: SubscriptionService,
    private val projectCardMapper: ProjectCardMapper,
) {

    private val logger = LoggerFactory.getLogger(ProjectService::class.java)

    private fun buildContentUrl(projectId: UUID, filePath: String): String {
        val normalizedPath = ProjectSnapshotValidator.normalizePath(filePath)
        return "$projectId/$normalizedPath"
    }

    fun existsById(projectId: UUID) : Boolean {
        return userProjectRepository.existsById(projectId)
    }

    private fun isBelowPlanLimit(userId: UUID): Boolean {

        val totalProjects = userProjectRepository.countByUserId(userId)
        val projectLimit = subscriptionService.getUserSubscriptionResponse(userId).maxProjects

        return totalProjects < projectLimit

    }

    internal fun getPublicProjects(page: Int, size: Int): ProjectCardListResponse {
        val pageable = PageRequest.of(page, size)

        val result = userProjectRepository.findPublicProjectCards(pageable)
        val technologiesByProjectId = getTechnologiesByProjectId(result.content.map { it.getProjectId() })

        logger.info(
            "${LogEvents.PROJECT_CARD_LIST_LOADED} {}",
            kv(LogFields.FILE_COUNT, result.content.size)
        )

        return projectCardMapper.toProjectCardResponseList(
            result.content,
            technologiesByProjectId,
            result.number,
            result.totalPages,
            result.hasNext()
        )
    }


    internal fun getUserProjects(userId: UUID, page: Int, size: Int): ProjectCardListResponse {
        val pageable = PageRequest.of(page, size)
        val result = userProjectRepository.findProjectCardsByUserId(userId, pageable)
        val technologiesByProjectId = getTechnologiesByProjectId(result.content.map { it.getProjectId() })

        logger.info(
            "${LogEvents.PROJECT_CARD_LIST_LOADED} {}",
            kv(LogFields.FILE_COUNT, result.size)
        )

        return projectCardMapper.toProjectCardResponseList(
            result.content,
            technologiesByProjectId,
            result.number,
            result.totalPages,
            result.hasNext()
        )

    }

    private fun getTechnologiesByProjectId(projectIds: List<UUID>): Map<UUID, List<String>> {
        if (projectIds.isEmpty()) {
            return emptyMap()
        }

        return projectFileRepository.findDistinctLanguagesByProjectIdIn(projectIds)
            .groupBy { it.getProjectId() }
            .mapValues { (_, languageRows) ->
                languageRows
                    .map { it.getCodeLanguage() }
                    .distinctBy { it.lowercase() }
            }
    }

    private fun validateLanguageAndPath(file: ProjectFileSnapshot) {
        try {
            Languages.validatePath(file.language, file.path)
        } catch (e: IllegalArgumentException) {
            val errorCode = if (e.message?.startsWith("Invalid language:") == true) {
                ErrorCode.LANGUAGE_NOT_FOUND
            } else {
                ErrorCode.INVALID_FILE_PATH
            }
            throw ApiException(errorCode, e.message ?: errorCode.defaultMessage)
        }
    }

    private fun normalizeAndValidateSubmittedFiles(
        entryFilePath: String,
        incomingFiles: List<ProjectFileSnapshot>
    ): Pair<String, List<ProjectFileSnapshot>> {
        val normalizedEntryFilePath = ProjectSnapshotValidator.normalizePath(entryFilePath)
        val normalizedFiles = ProjectSnapshotValidator.validateSnapshotRequest(normalizedEntryFilePath, incomingFiles)
        normalizedFiles.forEach(::validateLanguageAndPath)
        return normalizedEntryFilePath to normalizedFiles
    }

    @Transactional
    internal fun createProject(request: CreateProjectRequest, userId: UUID) {

        if (!isBelowPlanLimit(userId)) {
            throw ApiException(ErrorCode.PROJECT_LIMIT_EXCEEDED)
        }

        val projectName = request.projectName
        val requestHash = request.requestHash
        val projectType = request.projectType

        val (normalizedEntryFilePath, normalizedFiles) = normalizeAndValidateSubmittedFiles(
            request.entryFilePath,
            request.files
        )

        val now = OffsetDateTime.now(clock)

        val newProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = projectName,
                userId = userId,
                projectType = projectType,
                requestHash = requestHash,
                entryFilePath = normalizedEntryFilePath,
                createdAt = now,
                updatedAt = now,
            )
        )

        withMdc(LogFields.PROJECT_ID to newProject.id.toString()) {


            overwriteAllFiles(newProject.id, normalizedFiles)

            logger.info(
                LogEvents.PROJECT_CREATED + " {}",
                kv(LogFields.FILE_COUNT, normalizedFiles.size)
            )

        }
    }

    private fun getFirstFileName(base: String, extension: String): String {
        return base + extension
    }




    internal fun getProjectSnapshotForUserByProjectId(projectId: UUID, userId: UUID): ProjectSnapshot {
        val project = userProjectRepository.findById(projectId).orElseThrow{ ApiException(ErrorCode.PROJECT_NOT_FOUND) }
        val isOwnProject = project.userId == userId
        if (project.projectVisibility == Visibility.PRIVATE && !isOwnProject) {
            logger.warn(LogEvents.PROJECT_SNAPSHOT_FORBIDDEN)
            throw ApiException(ErrorCode.NOT_ALLOWED)
        }
        return getProjectSnapshotByProjectId(projectId)
    }

    fun getPublicProjectSnapshot(projectId: UUID, userId: UUID?): ProjectSnapshot {
        val project = userProjectRepository.findById(projectId).orElseThrow{ ApiException(ErrorCode.PROJECT_NOT_FOUND) }
        val isOwnProject = project.userId == userId
        if (project.projectVisibility == Visibility.PRIVATE && !isOwnProject) {
            logger.warn(LogEvents.PROJECT_SNAPSHOT_FORBIDDEN)
            throw ApiException(ErrorCode.NOT_ALLOWED)
        }
        return getProjectSnapshotByProjectId(projectId)
    }

    private fun getProjectSnapshotByProjectId(projectId: UUID): ProjectSnapshot {
        return withMdc(LogFields.PROJECT_ID to projectId.toString()) {
            val project = userProjectRepository.findById(projectId).orElseThrow()
            val lastUpdated = project.updatedAt
            val projectName = project.name
            val projectType = project.projectType
            val deleteAt = project.deleteAt
            val entryFilePath = project.entryFilePath
                ?: throw ApiException(ErrorCode.ENTRY_FILE_NOT_FOUND)
            val normalizedEntryFilePath = ProjectSnapshotValidator.normalizePath(entryFilePath)

            val projectFiles = projectFileRepository
                .findAllProjectFilesByProjectId(projectId)
                .sortedWith(compareByDescending {
                    ProjectSnapshotValidator.normalizePath(it.filePath) == normalizedEntryFilePath
                })
            val fileContentUrls = StorageGetRequest(projectFiles.map { it -> it.contentUrl })
            val fileContentsMap = storagePortForServices.getList(fileContentUrls)

            logger.info(
                "${LogEvents.PROJECT_SNAPSHOT_LOADED} {} {}",
                kv(LogFields.FILE_COUNT, projectFiles.size),
                kv(LogFields.HITS, fileContentsMap.content.size),
            )


            projectMapper.toProjectSnapshot(
                projectId,
                projectName,
                projectType,
                lastUpdated,
                deleteAt,
                projectFiles,
                fileContentsMap.content,
                normalizedEntryFilePath,
            )
        }
    }

    @Transactional
    internal fun duplicateProject(userId: UUID, projectId: UUID): UUID {

        if (!isBelowPlanLimit(userId)) {
            throw ApiException(ErrorCode.PROJECT_LIMIT_EXCEEDED)
        }

        val sourceProject = userProjectRepository.findById(projectId)
            .orElseThrow { ApiException(ErrorCode.PROJECT_NOT_FOUND) }

        if (userId != sourceProject.userId && sourceProject.projectVisibility == Visibility.PRIVATE) {
            throw ApiException(ErrorCode.NOT_OWN_PROJECT)
        }

        val now = OffsetDateTime.now(clock)
        val sourceFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)

        val duplicatedProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = sourceProject.name,
                userId = userId,
                requestHash = UUID.randomUUID(),
                deleteAt = null,
                projectVisibility = Visibility.PRIVATE,
                projectType = sourceProject.projectType,
                createdAt = now,
                updatedAt = now,
            )
        )

        withMdc(LogFields.PROJECT_ID to duplicatedProject.id.toString()) {
            val sourceContentMap = try {
                storagePortForServices.getList(StorageGetRequest(sourceFiles.map { it.contentUrl })).content
            } catch (e: Exception) {
                logger.error(
                    LogEvents.GCS_GET_FAILED + " {}",
                    kv(LogFields.FILE_COUNT, sourceFiles.size),
                    e
                )
                throw ApiException(ErrorCode.GCS_GET_FAILED, "Failed to get files from GCS: ${e.message}")
            }

            val uploadRequests = mutableListOf<StoragePutRequest>()

            for (sourceFile in sourceFiles) {
                val content = sourceContentMap[sourceFile.contentUrl]
                    ?: throw ApiException(
                        ErrorCode.STORAGE_OBJECT_NOT_FOUND,
                        "Could not find content for storage path: ${sourceFile.contentUrl}"
                    )

                val duplicatedFileId = UUID.randomUUID()
                val duplicatedContentUrl = buildContentUrl(duplicatedProject.id, sourceFile.filePath)

                uploadRequests.add(StoragePutRequest(path = duplicatedContentUrl, content = content))

                projectFileRepository.save(
                    ProjectFile(
                        id = duplicatedFileId,
                        projectId = duplicatedProject.id,
                        contentUrl = duplicatedContentUrl,
                        filePath = sourceFile.filePath,
                        codeLanguage = sourceFile.codeLanguage,
                    )
                )
            }

            val sourceEntryFilePath = sourceProject.entryFilePath
                ?: throw ApiException(ErrorCode.ENTRY_FILE_NOT_FOUND)
            val normalizedSourceEntryFilePath = ProjectSnapshotValidator.normalizePath(sourceEntryFilePath)
            val duplicatedEntryFilePath = sourceFiles
                .find { ProjectSnapshotValidator.normalizePath(it.filePath) == normalizedSourceEntryFilePath }
                ?.filePath
                ?.let(ProjectSnapshotValidator::normalizePath)
                ?: throw ApiException(
                    ErrorCode.ENTRY_FILE_NOT_FOUND,
                    "Source entry file is missing in duplicated files"
                )
            duplicatedProject.entryFilePath = duplicatedEntryFilePath

            try {
                storagePortForServices.uploadList(StoragePutRequestList(requests = uploadRequests))
            } catch (e: Exception) {
                logger.error(
                    LogEvents.STORAGE_UPLOAD_FAILED + " {}",
                    kv(LogFields.FILE_COUNT, uploadRequests.size),
                    e
                )
                throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload duplicated files to GCS: ${e.message}")
            }

            logger.info(
                LogEvents.PROJECT_CREATED + " {}",
                kv(LogFields.FILE_COUNT, sourceFiles.size)
            )
        }

        return duplicatedProject.id
    }

    internal fun deleteUserProjects(userId: UUID) {
        val projectIds = userProjectRepository.findAllByUserIdOrderByUpdatedAtDesc(userId)
        projectIds.forEach { it -> deleteProjectForUser(it.id, userId) }
    }

    @Transactional
    internal fun deleteProjectForUser(projectId: UUID, userId: UUID) {
        val existingProject = userProjectRepository.findById(projectId).orElseThrow()
        val existingFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)

        if (existingProject.userId != userId) {
            throw ApiException(ErrorCode.NOT_OWN_PROJECT)
        }

        logger.info(
            LogEvents.PROJECT_DELETE_REQUESTED + " {}",
            kv(LogFields.FILE_COUNT, existingFiles.size)
        )

        for (file in existingFiles) {
            projectFileRepository.deleteById(file.id)
        }

        userProjectRepository.deleteById(existingProject.id)
        deleteFiles(projectId, existingFiles)
    }

    private fun refreshUpdatedAt(existing: UserProject): UserProject {
        existing.updatedAt = OffsetDateTime.now(clock)
        return existing
    }

    private fun refreshUpdatedAt(existingId: UUID) {
        var existingProject = userProjectRepository.findById(existingId).orElseThrow()
        existingProject.updatedAt = OffsetDateTime.now(clock)
        userProjectRepository.save(existingProject)
    }

    @Transactional
    internal fun renameProject(renameProjectRequest: RenameProjectRequest, userId: UUID) {

        val projectId = renameProjectRequest.targetId
        val newName = renameProjectRequest.newName
        logger.info(
            LogEvents.PROJECT_RENAME_REQUESTED + " {}",
            kv(LogFields.NAME_LENGTH, newName.length)
        )

        var existingProject = userProjectRepository.findById(projectId).orElseThrow()

        if (existingProject.userId != userId) {
            throw ApiException(ErrorCode.NOT_OWN_PROJECT)
        }

        existingProject.name = newName
        existingProject = refreshUpdatedAt(existingProject)
        userProjectRepository.save(existingProject)

    }

    @Transactional
    internal fun changeProjectVisibility(projectId: UUID, userId: UUID, value: Visibility) {
        val existingProject = userProjectRepository.findById(projectId).orElseThrow { ApiException(ErrorCode.PROJECT_NOT_FOUND) }

        if (existingProject.userId != userId) {
            throw ApiException(ErrorCode.NOT_OWN_PROJECT)
        }

        existingProject.projectVisibility = value

    }


    @Transactional
    internal fun saveProjectSnapshot(projectSnapshot: ProjectSnapshot, userId: UUID): ProjectSnapshot {
        val projectId = projectSnapshot.projectId

        return withMdc(LogFields.PROJECT_ID to projectId.toString()) {
            val existingProject = userProjectRepository.findById(projectId)
                .orElseThrow { ApiException(ErrorCode.PROJECT_NOT_FOUND) }

            if (existingProject.userId != userId) {
                throw ApiException(ErrorCode.NOT_OWN_PROJECT)
            }

            val existingFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)
            val (normalizedEntryPath, normalizedSubmittedFiles) = normalizeAndValidateSubmittedFiles(
                projectSnapshot.entryFilePath,
                projectSnapshot.files
            )

            val existingFileIdByPath = existingFiles.associate { file ->
                ProjectSnapshotValidator.normalizePath(file.filePath) to file.id
            }


            val resolvedSubmittedFiles = normalizedSubmittedFiles.map { file ->
                val normalizedPath = ProjectSnapshotValidator.normalizePath(file.path)
                file.copy(id = existingFileIdByPath[normalizedPath])
            }

            val backupContents = fetchProjectFileContents(existingFiles)

            existingProject.entryFilePath = normalizedEntryPath

            try {
                deleteFiles(projectId, existingFiles)
                overwriteAllFiles(projectId, resolvedSubmittedFiles)
            } catch (e: Exception) {
                restoreProjectFiles(projectId, existingFiles, backupContents)
                throw e
            }

            refreshUpdatedAt(projectId)

            getProjectSnapshotByProjectId(projectId)
        }
    }

    private fun overwriteAllFiles(projectId: UUID, files: List<ProjectFileSnapshot>) {

        val gcsRequests = mutableListOf<StoragePutRequest>()

        for (file in files) {

            val fileId = file.id ?: UUID.randomUUID()
            val normalizedPath = ProjectSnapshotValidator.normalizePath(file.path)
            val contentUrl = buildContentUrl(projectId, file.path)
            val languageName = file.language

            gcsRequests.add(StoragePutRequest(contentUrl, file.content))


            projectFileRepository.save(
                ProjectFile(
                    id = fileId,
                    projectId = projectId,
                    contentUrl = contentUrl,
                    filePath = normalizedPath,
                    codeLanguage = languageName
                )
            )
        }

        try {
            storagePortForServices.uploadList(StoragePutRequestList(gcsRequests))
        } catch (e: Exception) {
            logger.error(
                LogEvents.STORAGE_UPLOAD_FAILED + " {} {}",
                kv(LogFields.PROJECT_ID, projectId.toString()),
                kv(LogFields.FILE_COUNT, gcsRequests.size),
                e
            )
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED)
        }
    }

    private fun fetchProjectFileContents(projectFiles: List<ProjectFile>): Map<String, String> {
        if (projectFiles.isEmpty()) return emptyMap()

        return try {
            storagePortForServices
                .getList(StorageGetRequest(projectFiles.map { it.contentUrl }))
                .content
        } catch (e: Exception) {
            logger.error(
                LogEvents.GCS_GET_FAILED + " {}",
                kv(LogFields.FILE_COUNT, projectFiles.size),
                e
            )
            throw ApiException(ErrorCode.GCS_GET_FAILED, "Failed to back up files from GCS: ${e.message}")
        }
    }

    private fun restoreProjectFiles(projectId: UUID, previousFiles: List<ProjectFile>, previousContents: Map<String, String>) {
        logger.warn(
            "snapshot_save_restore_started {}",
            kv(LogFields.PROJECT_ID, projectId.toString())
        )

        val currentFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)
        currentFiles.forEach { projectFileRepository.deleteById(it.id) }

        previousFiles.forEach { projectFileRepository.save(it) }

        val restoreRequests = previousFiles.mapNotNull { file ->
            previousContents[file.contentUrl]?.let { content ->
                StoragePutRequest(path = file.contentUrl, content = content)
            }
        }

        if (restoreRequests.isNotEmpty()) {
            try {
                storagePortForServices.uploadList(StoragePutRequestList(requests = restoreRequests))
            } catch (e: Exception) {
                logger.error(
                    "snapshot_save_restore_failed {} {}",
                    kv(LogFields.PROJECT_ID, projectId.toString()),
                    kv(LogFields.FILE_COUNT, restoreRequests.size),
                    e
                )
                throw ApiException(ErrorCode.GCS_RESTORE_FAILED, "Failed to restore project files to storage after a failed save: ${e.message}")
            }
        }
    }

    private fun deleteFiles(projectId: UUID, projectFilesToDelete: List<ProjectFile>) {

        val toDeletePaths = projectFilesToDelete.map { it -> it.contentUrl }

        for (file in projectFilesToDelete) {

            projectFileRepository.deleteById(file.id)
        }

        val gcsDeleteRequest = StorageDeleteRequest(toDeletePaths)

        try {
            storagePortForServices.deleteList(gcsDeleteRequest)
        } catch (e: Exception) {
            logger.error(
                LogEvents.STORAGE_DELETE_FAILED + " {}",
                kv(LogFields.PROJECT_ID, projectId.toString()),
                kv(LogFields.FILE_COUNT, toDeletePaths.size),
                e
            )
            throw ApiException(ErrorCode.GCS_DELETE_FAILED, "Failed to delete files from storage: ${e.message}")
        }

    }

}

