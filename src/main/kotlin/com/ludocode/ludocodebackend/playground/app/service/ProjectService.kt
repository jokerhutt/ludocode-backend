package com.ludocode.ludocodebackend.playground.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.util.sha256
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequest
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.port.`in`.GcsPortForPlayground
import com.ludocode.ludocodebackend.playground.app.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.playground.app.dto.request.RenameRequest
import com.ludocode.ludocodebackend.playground.app.mapper.ProjectMapper
import com.ludocode.ludocodebackend.playground.app.port.`in`.ProjectsPortForAI
import com.ludocode.ludocodebackend.playground.app.util.ProjectSnapshotDiffer
import com.ludocode.ludocodebackend.playground.app.util.ProjectSnapshotValidator
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import com.ludocode.ludocodebackend.playground.infra.repository.ProjectFileRepository
import com.ludocode.ludocodebackend.playground.infra.repository.UserProjectRepository
import jakarta.transaction.Transactional
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@ConditionalOnProperty(prefix = "gcs", name = ["enabled"], havingValue = "true")
@Service
class ProjectService(
    private val userProjectRepository: UserProjectRepository,
    private val projectFileRepository: ProjectFileRepository,
    private val projectMapper: ProjectMapper,
    private val clock: Clock,

    private val gcsPortForPlayground: GcsPortForPlayground,
) : ProjectsPortForAI {

    @Transactional
    internal fun createProject(request: CreateProjectRequest, userId: UUID) : ProjectListResponse {

        val projectName = request.projectName
        val language = request.projectLanguage
        val requestHash = request.requestHash

        val newProject = userProjectRepository.save(UserProject(
            id = UUID.randomUUID(),
            name = projectName,
            userId = userId,
            requestHash = requestHash,
            projectLanguage = language,
            createdAt = OffsetDateTime.now(clock),
            updatedAt = OffsetDateTime.now(clock)
        ))

        val firstFileName = getFirstFileName(language)
        val firstFileId = UUID.randomUUID()
        val firstFileContentUrl = "${newProject.id}/$firstFileId"
        val firstFileContent = "print('Hello Mimo!')"
        projectFileRepository.save(ProjectFile(
            id = firstFileId,
            projectId = newProject.id,
            contentUrl = firstFileContentUrl,
            filePath = firstFileName,
            fileLanguage = language,
            contentHash = sha256(firstFileContent)
        ))

        try {
            gcsPortForPlayground.uploadDataList(GcsPutRequestList(requests = listOf(GcsPutRequest(path = firstFileContentUrl, content = firstFileContent))))
        } catch (e: Exception) {
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
        }

        return getUserProjects(userId)

    }

    private fun getFirstFileName (language : LanguageType): String {
        val base = "script."
        val name = when (language) {
            LanguageType.python -> "py"
            LanguageType.javascript -> "js"
            LanguageType.lua -> "lua"
        }
        return base + name
    }

    internal fun getUserProjects(userId: UUID) : ProjectListResponse {
        val projectIds = userProjectRepository.findProjectIdsByUserId(userId)
        val projectSnapshots = mutableListOf<ProjectSnapshot>()
        for (projectId in projectIds) {
            projectSnapshots.add(getProjectSnapshotForUserByProjectId(projectId, userId))
        }
        println(
            projectSnapshots.joinToString("\n") {
                "projectId=${it.projectId}, name=${it.projectName}"
            }
        )
        return ProjectListResponse(projectSnapshots)
    }

    override fun getFileContentById(fileId: UUID): String {
        val file = projectFileRepository.findById(fileId)
            .orElseThrow { ApiException(ErrorCode.PROJECT_NOT_FOUND) }
        println("FOUND FILE")

        try {
            return gcsPortForPlayground.getContentFromPath(file.contentUrl)
        } catch (e: Exception) {
            throw ApiException(ErrorCode.GCS_GET_FAILED, "Failed to get files from GCS: ${e.message}")
        }

    }

    internal fun getProjectSnapshotForUserByProjectId (projectId: UUID, userId: UUID) : ProjectSnapshot {
        val project = userProjectRepository.findById(projectId).orElseThrow()
        val isOwnProject = project.userId == userId
        if (!isOwnProject) throw ApiException(ErrorCode.NOT_ALLOWED)
        return getProjectSnapshotByProjectId(projectId)
    }

    private fun getProjectSnapshotByProjectId (projectId: UUID) : ProjectSnapshot {

        val project = userProjectRepository.findById(projectId).orElseThrow()
        val projectName = project.name
        val projectLanguage = project.projectLanguage
        val projectFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)
        val fileContentUrls = projectFiles.map { it -> it.contentUrl }
        val fileContentsMap = gcsPortForPlayground.getContentFromUrls(fileContentUrls)

        return projectMapper.toProjectSnapshot(projectId, projectName, projectLanguage, projectFiles, fileContentsMap)
    }

    @Transactional
    internal fun deleteProjectForUser (projectId: UUID, userId: UUID) : ProjectListResponse {

        val existingProject = userProjectRepository.findById(projectId).orElseThrow()
        val existingFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)

        for (file in existingFiles) {
            projectFileRepository.deleteById(file.id)
        }
        userProjectRepository.deleteById(existingProject.id)

        deleteFiles(projectId, existingFiles)

        return getUserProjects(userId)

    }

    @Transactional
    internal fun renameProject (renameRequest: RenameRequest, userId: UUID) : ProjectListResponse {

        val projectId = renameRequest.targetId
        val newName = renameRequest.newName

        var existingProject = userProjectRepository.findById(projectId).orElseThrow()
        existingProject.name = newName
        existingProject.updatedAt = OffsetDateTime.now(clock)
        userProjectRepository.save(existingProject)

        return getUserProjects(userId)

    }

    @Transactional
    internal fun saveProjectSnapshot (projectSnapshot: ProjectSnapshot): ProjectSnapshot {

        if (!userProjectRepository.existsById(projectSnapshot.projectId)) throw ApiException(ErrorCode.PROJECT_NOT_FOUND, "This project doesnt exist")

        val submittedFiles = projectSnapshot.files

        ProjectSnapshotValidator.validateSnapshotRequest(submittedFiles)

        val existingFiles : List<ProjectFile> = projectFileRepository.findAllProjectFilesByProjectId(projectSnapshot.projectId)

        val snapshotDiff = ProjectSnapshotDiffer.computeSnapshotDiff(submittedFiles, existingFiles)

        val projectId = projectSnapshot.projectId

            deleteFiles(projectId, snapshotDiff.toDeleteFiles)
            updateChangedFiles(projectId, snapshotDiff.toUpdate)
            saveNewFiles(projectId, snapshotDiff.toAdd)

        return getProjectSnapshotByProjectId(projectId)

    }



    private fun deleteFiles (projectId: UUID, projectFilesToDelete: List<ProjectFile>) {

        val toDeletePaths = projectFilesToDelete.map { it -> it.contentUrl }

        for (file in projectFilesToDelete) {
            projectFileRepository.deleteById(file.id)
        }

        val gcsDeleteRequest = GcsDeleteRequestList(toDeletePaths)

        try {
            gcsPortForPlayground.deleteDataList(gcsDeleteRequest)
        } catch (e: Exception) {
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to delete files to GCS: ${e.message}")
        }

    }

    private fun saveNewFiles (projectId: UUID, files: List<ProjectFileSnapshot>) {

        val gcsRequests = mutableListOf<GcsPutRequest>()

        for (file in files) {
            val hash = sha256(file.content)

            val fileId = file.id ?: UUID.randomUUID()

            val contentUrl = "$projectId/${fileId}"
            gcsRequests.add(GcsPutRequest(contentUrl, file.content))
            projectFileRepository.save(ProjectFile(
                id = fileId,
                projectId = projectId,
                contentUrl = contentUrl,
                filePath = file.path,
                contentHash = hash,
                fileLanguage = file.language
            ))
        }

        try {
            gcsPortForPlayground.uploadDataList(GcsPutRequestList(requests = gcsRequests))
        } catch (e: Exception) {
            println("Failed")
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
        }

    }

    private fun updateChangedFiles (projectId: UUID, files: List<ProjectFileSnapshot>) {

        val gcsRequests = mutableListOf<GcsPutRequest>()

        for (file in files) {
            if (file.id == null) throw ApiException(ErrorCode.PROJECT_FILE_ID_NULL, "The Project file id is null for an existing file")

            val contentUrl = "$projectId/${file.id}"
            gcsRequests.add(GcsPutRequest(contentUrl, file.content))

            val existingFile = projectFileRepository.findById(file.id).orElseThrow()

            val newHash = sha256(file.content)
            val newPath = file.path

            existingFile.contentHash = newHash
            existingFile.filePath = newPath

            projectFileRepository.save(existingFile)
        }

        try {
            gcsPortForPlayground.uploadDataList(GcsPutRequestList(requests = gcsRequests))
        } catch (e: Exception) {
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
        }

    }

}