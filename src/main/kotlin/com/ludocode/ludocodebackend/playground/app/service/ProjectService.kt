package com.ludocode.ludocodebackend.playground.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequest
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.playground.app.dto.internal.ProjectSnapshotDiff
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.mapper.ProjectMapper
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.playground.infra.http.GcsClientForPlayground
import com.ludocode.ludocodebackend.playground.infra.repository.ProjectFileRepository
import com.ludocode.ludocodebackend.playground.infra.repository.UserProjectRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.util.UUID


@Service
class ProjectService(
    private val userProjectRepository: UserProjectRepository,
    private val projectFileRepository: ProjectFileRepository,
    private val gcsClientForPlayground: GcsClientForPlayground,
    private val projectMapper: ProjectMapper,
) {


    fun getProjectSnapshotByProjectId (projectId: UUID) : ProjectSnapshot {

        println("Proj serv ch 1")

        val bucketName = "ludo-file-content"
        val projectName = userProjectRepository.getProjectNameById(projectId)
        val projectFiles = projectFileRepository.findAllProjectFilesByProjectId(projectId)
        val fileContentUrls = projectFiles.map { it -> it.contentUrl }
        val fileContentsMap = gcsClientForPlayground.getContentFromUrls(fileContentUrls)

        println("Proj serv ch 2")

        return projectMapper.toProjectSnapshot(projectId, projectName, projectFiles, fileContentsMap)
    }

    @Transactional
    fun saveProjectSnapshot (projectSnapshot: ProjectSnapshot): ProjectSnapshot {

        println("In save project")
        if (!userProjectRepository.existsById(projectSnapshot.projectId)) throw ApiException(ErrorCode.PROJECT_NOT_FOUND, "This project doesnt exist")

        val submittedFiles = projectSnapshot.files

        println("validating snapshot")
        validateSnapshotRequest(submittedFiles)

        val existingFiles : List<ProjectFile> = projectFileRepository.findAllProjectFilesByProjectId(projectSnapshot.projectId)

        val snapshotDiff = computeSnapshotDiff(submittedFiles, existingFiles)

        val projectId = projectSnapshot.projectId

            deleteFiles(projectId, snapshotDiff.toDeleteFiles)
            updateChangedFiles(projectId, snapshotDiff.toUpdate)
            saveNewFiles(projectId, snapshotDiff.toAdd)

        return getProjectSnapshotByProjectId(projectId)



    }

    private fun validateSnapshotRequest (incoming: List<ProjectFileSnapshot>) {
        require(incoming.isNotEmpty()) { "No files submitted" }
        println("Validation A")
        require(incoming.size == incoming.map { it.path }.toSet().size) { "Duplicate filenames in snapshot" }
        println("Validation B")
        incoming.forEach { f ->
            require(f.path.matches(Regex("""^[\w\-.]+$"""))) { "Bad filename: ${f.path}" }
            require(f.content.length <= 512_000) { "File too large: ${f.path}" }
        }
        println("Validation C")
    }

    private fun computeSnapshotDiff (incomingFiles: List<ProjectFileSnapshot>, existingFiles: List<ProjectFile>): ProjectSnapshotDiff {


        val filesToAdd = mutableListOf<ProjectFileSnapshot>()
        val filesToUpdate = mutableListOf<ProjectFileSnapshot>()
        val filesToDelete = mutableListOf<ProjectFile>()
        val remainingFileIds = mutableListOf<UUID>()

        println("Computing diff")

        val incomingIds = incomingFiles.mapNotNull { it.id }.toSet()
        val existingFileMap = existingFiles
            .filter { it.id in incomingIds }  // ← ONLY files with ID in incoming
            .associateBy { it.id }
        filesToDelete.addAll(existingFiles.map { it }.filter { it.id !in incomingIds })
        val filesToDeleteIds = filesToDelete.map { it.id }

        val filteredIncoming = incomingFiles.filter { it.id !in filesToDeleteIds }

        println("Entering loop")

        for (incoming in filteredIncoming) {

            if (incoming.id == null) {
                println("Id null, adding")
                filesToAdd.add(incoming)
                continue
            }

            val existingFile = existingFileMap[incoming.id]

            if (existingFile == null) {
                println("Existing null, adding")
                filesToAdd.add(incoming)
                continue
            }

            if (hasFileChanged(incoming, existingFile)) {
                println("File changed, updating " +  incoming.id)

                filesToUpdate.add(incoming)
            } else {
                println("no change " + incoming.id)
                remainingFileIds.add(incoming.id)
            }

        }

        println("ToDelete size: " + filesToDelete.size)
        println("ToUpdate size: " + filesToUpdate.size)
        println("Remaining size: " + filesToDelete.size)
        println("ToAdd size: " + filesToAdd.size)



        return ProjectSnapshotDiff(remainingFileIds = remainingFileIds, toAdd = filesToAdd, toDeleteFiles = filesToDelete, toUpdate = filesToUpdate )

    }

    @Transactional
    fun deleteFiles (projectId: UUID, projectFilesToDelete: List<ProjectFile>) {

        val toDeletePaths = projectFilesToDelete.map { it -> it.contentUrl }

        for (file in projectFilesToDelete) {
            projectFileRepository.deleteById(file.id)
        }

        val gcsDeleteRequest = GcsDeleteRequestList(toDeletePaths)

        try {
            gcsClientForPlayground.deleteDataList(gcsDeleteRequest)
        } catch (e: Exception) {
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to delete files to GCS: ${e.message}")
        }

    }

    @Transactional
    fun saveNewFiles (projectId: UUID, files: List<ProjectFileSnapshot>) {

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
            gcsClientForPlayground.uploadDataList(GcsPutRequestList(requests = gcsRequests))
        } catch (e: Exception) {
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
        }

    }

    @Transactional
    fun updateChangedFiles (projectId: UUID, files: List<ProjectFileSnapshot>) {

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
            gcsClientForPlayground.uploadDataList(GcsPutRequestList(requests = gcsRequests))
        } catch (e: Exception) {
            throw ApiException(ErrorCode.GCS_UPLOAD_FAILED, "Failed to upload files to GCS: ${e.message}")
        }

    }

    private fun hasFileChanged (incomingFile: ProjectFileSnapshot, existingFile: ProjectFile) : Boolean {
        if (incomingFile.path != existingFile.filePath) return true
        val incomingHash = sha256(incomingFile.content)
        return incomingHash != existingFile.contentHash
    }

    fun sha256(text: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }









}