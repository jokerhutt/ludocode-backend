package com.ludocode.ludocodebackend.projects.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.commons.util.sha256
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.request.RenameProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectSnapshotDiff
import com.ludocode.ludocodebackend.projects.app.mapper.ProjectMapper
import com.ludocode.ludocodebackend.projects.app.util.ProjectSnapshotDiffer
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
    private val codeLanguagesRepository: CodeLanguagesRepository,
    private val subscriptionService: SubscriptionService,
) {

    private val logger = LoggerFactory.getLogger(ProjectService::class.java)

    private fun isBelowPlanLimit(userId: UUID): Boolean {

        val totalProjects = userProjectRepository.countByUserId(userId)
        val projectLimit = subscriptionService.getUserSubscriptionResponse(userId).maxProjects

        return totalProjects < projectLimit

    }

    @Transactional
    internal fun createProject(request: CreateProjectRequest, userId: UUID): ProjectListResponse {

        if (!isBelowPlanLimit(userId)) {
            throw ApiException(ErrorCode.PROJECT_LIMIT_EXCEEDED)
        }

        val projectName = request.projectName
        val requestHash = request.requestHash
        val languageId = request.projectLanguageId


        val codeLanguage = codeLanguagesRepository.findById(languageId)
            .orElseThrow { ApiException(ErrorCode.LANGUAGE_NOT_FOUND) }

        val newProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = projectName,
                userId = userId,
                requestHash = requestHash,
                codeLanguage = codeLanguage,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock)
            )
        )

        withMdc(LogFields.PROJECT_ID to newProject.id.toString()) {

            val firstFileName = getFirstFileName(base = codeLanguage.base, extension = codeLanguage.extension)
            val firstFileId = UUID.randomUUID()
            val firstFileContentUrl = "${newProject.id}/$firstFileId"
            val firstFileContent = codeLanguage.initialScript ?: ""
            projectFileRepository.save(
                ProjectFile(
                    id = firstFileId,
                    projectId = newProject.id,
                    contentUrl = firstFileContentUrl,
                    filePath = firstFileName,
                    codeLanguage = codeLanguage,
                    contentHash = sha256(firstFileContent)
                )
            )

            newProject.entryFileId = firstFileId

            try {
                storagePortForServices.uploadList(
                    StoragePutRequestList(
                        requests = listOf(
                            StoragePutRequest(
                                path = firstFileContentUrl,
                                content = firstFileContent
                            )
                        )
                    )
                )
            } catch (e: Exception) {
                logger.error(
                    LogEvents.STORAGE_UPLOAD_FAILED + " {}",
                    kv(LogFields.FILE_COUNT, 1),
                    e
                )
                throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
            }

            logger.info(
                LogEvents.PROJECT_CREATED + " {}",
                kv(LogFields.LANGUAGE, codeLanguage.name)
            )

        }
        return getUserProjects(userId)


    }

    private fun getFirstFileName(base: String, extension: String): String {
        return base + extension
    }

    internal fun getUserProjects(userId: UUID): ProjectListResponse {
        val projectIds = userProjectRepository.findProjectIdsByUserId(userId)

        logger.info(
            "${LogEvents.PROJECT_SNAPSHOT_LIST_LOADED} {}",
            kv(LogFields.FILE_COUNT, projectIds.size)
        )

        val projectSnapshots = mutableListOf<ProjectSnapshot>()
        for (projectId in projectIds) {
            projectSnapshots.add(getProjectSnapshotForUserByProjectId(projectId, userId))
        }
        return ProjectListResponse(projectSnapshots)
    }

    internal fun getProjectSnapshotForUserByProjectId(projectId: UUID, userId: UUID): ProjectSnapshot {
        val project = userProjectRepository.findById(projectId).orElseThrow()
        val isOwnProject = project.userId == userId
        if (!isOwnProject) {
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
            val deleteAt = project.deleteAt
            val projectLanguage = project.codeLanguage
            val visibility = project.projectVisibility
            val entryFileId = project.entryFileId
                ?: throw ApiException(ErrorCode.ENTRY_FILE_NOT_FOUND)

            val projectFiles = projectFileRepository
                .findAllProjectFilesByProjectId(projectId)
                .sortedWith(compareByDescending { it.id == entryFileId })
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
                projectLanguage,
                lastUpdated,
                deleteAt,
                projectFiles,
                fileContentsMap.content,
                entryFileId,
                visibility = visibility
            )
        }
    }

    @Transactional
    internal fun deleteProjectForUser(projectId: UUID, userId: UUID): ProjectListResponse {
        val existingProject = userProjectRepository.findById(projectId).orElseThrow()
        val existingFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)

        logger.info(
            LogEvents.PROJECT_DELETE_REQUESTED + " {}",
            kv(LogFields.FILE_COUNT, existingFiles.size)
        )

        for (file in existingFiles) {
            projectFileRepository.deleteById(file.id)
        }

        userProjectRepository.deleteById(existingProject.id)
        deleteFiles(projectId, existingFiles)

        return getUserProjects(userId)
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
    internal fun renameProject(renameProjectRequest: RenameProjectRequest, userId: UUID): ProjectListResponse {

        val projectId = renameProjectRequest.targetId
        val newName = renameProjectRequest.newName
        logger.info(
            LogEvents.PROJECT_RENAME_REQUESTED + " {}",
            kv(LogFields.NAME_LENGTH, newName.length)
        )

        var existingProject = userProjectRepository.findById(projectId).orElseThrow()
        existingProject.name = newName
        existingProject = refreshUpdatedAt(existingProject)
        userProjectRepository.save(existingProject)

        return getUserProjects(userId)

    }

    @Transactional
    internal fun changeProjectVisibility(projectId: UUID, userId: UUID, value: Visibility) {
        val project = userProjectRepository.findById(projectId).orElseThrow { ApiException(ErrorCode.PROJECT_NOT_FOUND) }

        if (project.userId != userId) {
            throw ApiException(ErrorCode.NOT_OWN_PROJECT)
        }

        project.projectVisibility = value

    }


    @Transactional
    internal fun saveProjectSnapshot(projectSnapshot: ProjectSnapshot): ProjectSnapshot {
        val projectId = projectSnapshot.projectId

        return withMdc(LogFields.PROJECT_ID to projectId.toString()) {
            if (!userProjectRepository.existsById(projectId)) throw ApiException(
                ErrorCode.PROJECT_NOT_FOUND,
                "This project doesnt exist"
            )

            val submittedFiles = projectSnapshot.files
            val entryFileId = projectSnapshot.entryFileId

            ProjectSnapshotValidator.validateSnapshotRequest(entryFileId, submittedFiles)

            val existingFiles: List<ProjectFile> = projectFileRepository.findAllProjectFilesByProjectId(projectId)

            val snapshotDiff = ProjectSnapshotDiffer.computeSnapshotDiff(submittedFiles, existingFiles)

            logger.info(
                LogEvents.PROJECT_SNAPSHOT_DIFF + " {} {} {}",
                kv(LogFields.ADD_COUNT, snapshotDiff.toAdd.size),
                kv(LogFields.UPDATE_COUNT, snapshotDiff.toUpdate.size),
                kv(LogFields.DELETE_COUNT, snapshotDiff.toDeleteFiles.size)
            )

            if (hasProjectChanged(snapshotDiff)) {
                refreshUpdatedAt(projectId)
            }

            deleteFiles(projectId, snapshotDiff.toDeleteFiles)
            updateChangedFiles(projectId, snapshotDiff.toUpdate)
            saveNewFiles(projectId, snapshotDiff.toAdd)

            getProjectSnapshotByProjectId(projectId)
        }


    }

    private fun hasProjectChanged(projectSnapshotDiff: ProjectSnapshotDiff): Boolean {
        if (projectSnapshotDiff.toAdd.isNotEmpty()) return true
        if (projectSnapshotDiff.toUpdate.isNotEmpty()) return true
        if (projectSnapshotDiff.toDeleteFiles.isNotEmpty()) return true
        return false
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
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to delete files to GCS: ${e.message}")
        }

    }

    private fun saveNewFiles(projectId: UUID, files: List<ProjectFileSnapshot>) {

        val gcsRequests = mutableListOf<StoragePutRequest>()

        for (file in files) {
            val hash = sha256(file.content)

            val fileId = file.id ?: UUID.randomUUID()

            val contentUrl = "$projectId/${fileId}"
            gcsRequests.add(StoragePutRequest(contentUrl, file.content))

            val language = codeLanguagesRepository.getReferenceById(file.language.languageId)

            projectFileRepository.save(
                ProjectFile(
                    id = fileId,
                    projectId = projectId,
                    contentUrl = contentUrl,
                    filePath = file.path,
                    contentHash = hash,
                    codeLanguage = language
                )
            )
        }

        try {
            storagePortForServices.uploadList(StoragePutRequestList(requests = gcsRequests))
        } catch (e: Exception) {
            logger.error(
                LogEvents.STORAGE_UPLOAD_FAILED + " {} {}",
                kv(LogFields.PROJECT_ID, projectId.toString()),
                kv(LogFields.FILE_COUNT, gcsRequests.size),
                e
            )
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
        }

    }

    private fun updateChangedFiles(projectId: UUID, files: List<ProjectFileSnapshot>) {

        val gcsRequests = mutableListOf<StoragePutRequest>()

        for (file in files) {
            if (file.id == null) throw ApiException(
                ErrorCode.PROJECT_FILE_ID_NULL,
                "The Project file id is null for an existing file"
            )


            val contentUrl = "$projectId/${file.id}"
            gcsRequests.add(StoragePutRequest(contentUrl, file.content))

            val existingFile = projectFileRepository.findById(file.id)
                .orElseThrow { ApiException(ErrorCode.PROJECT_FILE_NOT_FOUND) }

            val newHash = sha256(file.content)
            val newPath = file.path

            existingFile.contentHash = newHash
            existingFile.filePath = newPath

            projectFileRepository.save(existingFile)
        }

        try {
            storagePortForServices.uploadList(StoragePutRequestList(requests = gcsRequests))
        } catch (e: Exception) {
            logger.error(
                LogEvents.STORAGE_UPLOAD_FAILED + " {} {}",
                kv(LogFields.PROJECT_ID, projectId.toString()),
                kv(LogFields.FILE_COUNT, gcsRequests.size),
                e
            )
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
        }

    }

}