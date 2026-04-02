package com.ludocode.ludocodebackend.project.integration

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.request.RenameProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardListResponse
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.context.junit.jupiter.EnabledIf
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

@EnabledIf(
    expression = "#{environment.getProperty('storage.mode') == 'gcs'}",
    loadContext = true
)
class UserProjectIT : AbstractIntegrationTest() {

    private lateinit var existingProject: UserProject

    @BeforeEach
    fun seed() {
        existingProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = "Untitled",
                userId = user1.id!!,
                createdAt = OffsetDateTime.now(clock).minusDays(2),
                updatedAt = OffsetDateTime.now(clock).minusDays(1),
                requestHash = UUID.randomUUID(),
                projectType = ProjectType.CODE,
                entryFilePath = "script.py",
            )
        )

        val projectId = existingProject.id
        val f1Path = "script.py"
        val f2Path = "script-1.py"
        val f1Url = "$projectId/$f1Path"
        val f2Url = "$projectId/$f2Path"
        val f1Content = "print('hello world!')"
        val f2Content = "print('bye world!')"

        projectFileRepository.saveAll(
            listOf(
                ProjectFile(
                    id = UUID.randomUUID(),
                    projectId = existingProject.id,
                    contentUrl = f1Url,
                    filePath = f1Path,
                    codeLanguage = pythonLanguage
                ),
                ProjectFile(
                    id = UUID.randomUUID(),
                    projectId = existingProject.id,
                    contentUrl = f2Url,
                    filePath = f2Path,
                    codeLanguage = pythonLanguage
                )
            )
        )

        try {
            storage.create(BucketInfo.of("lumo-file-content"))
        } catch (_: Exception) {
        }

        storage.list("lumo-file-content").iterateAll().forEach { blob -> storage.delete(blob.blobId) }
        storage.create(BlobInfo.newBuilder("lumo-file-content", f1Url).build(), f1Content.toByteArray())
        storage.create(BlobInfo.newBuilder("lumo-file-content", f2Url).build(), f2Content.toByteArray())
    }

    @Test
    fun createPythonProject_createsNew_returnsNewProjectsList() {
        submitPostCreateProject(newProjectRequest("Second Project", "script.py", pythonLanguage, "print('Hello World!')"), user1.id!!)

        val response = submitGetUserProjects(user1.id!!)
        assertThat(response.projects).hasSize(2)
        val newProject = response.projects.first { it.projectTitle == "Second Project" }

        val snapshotRes = submitGetProjectSnapshot(newProject.projectId, user1.id!!)
        assertThat(snapshotRes.files).hasSize(1)
        assertThat(snapshotRes.files[0].content).isEqualTo("print('Hello World!')")
        assertThat(snapshotRes.files[0].path).isEqualTo("script.py")
    }

    @Test
    fun createJsProject_createsNew_returnsNewProjectsList() {
        submitPostCreateProject(newProjectRequest("Third Project", "script.js", jsLanguage, "console.log('Hello World!')"), user1.id!!)

        val response = submitGetUserProjects(user1.id!!)
        assertThat(response.projects).hasSize(2)
        val newProject = response.projects.first { it.projectTitle == "Third Project" }

        val snapshotRes = submitGetProjectSnapshot(newProject.projectId, user1.id!!)
        assertThat(snapshotRes.files).hasSize(1)
        assertThat(snapshotRes.files[0].content).isEqualTo("console.log('Hello World!')")
        assertThat(snapshotRes.files[0].path).isEqualTo("script.js")
    }

    @Test
    fun deleteProject_deletesOnlyProject_returnsEmptyList() {
        submitDeleteProject(existingProject.id, user1.id!!)
        val res = submitGetUserProjects(user1.id!!)
        assertThat(res.projects).isEmpty()
    }

    @Test
    fun renameProject_renamesProject_returnsRenamed() {
        val request = RenameProjectRequest(targetId = existingProject.id, newName = "Test Project Name")
        submitPatchRenameProject(request, user1.id!!)

        val res = submitGetUserProjects(user1.id!!)
        assertThat(res.projects).hasSize(1)
        assertThat(res.projects[0].projectId).isEqualTo(existingProject.id)
        assertThat(res.projects[0].projectTitle).isEqualTo("Test Project Name")
    }

    @Test
    fun saveProject_deleteAddAndRename_returnsSuccess() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val modifiedFiles = snapshot.files.toMutableList()

        modifiedFiles.removeAt(1)
        modifiedFiles.add(ProjectFileSnapshot(null, "script-2.py", pythonLanguage, "print(2 + 2)"))
        modifiedFiles[0] = modifiedFiles[0].copy(content = "print('Awesome')")

        val res = submitPutSaveProject(user1.id!!, snapshot.copy(files = modifiedFiles))
        assertThat(res.projectId).isEqualTo(existingProject.id)
        assertThat(res.files).hasSize(2)
        assertThat(res.files.map { it.path }).containsExactly("script.py", "script-2.py")
    }

    @Test
    fun saveProject_renameEntryFileAndUpdateEntryFilePath_returnsSuccess() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles[0] = modifiedFiles[0].copy(path = "app.py")

        val res = submitPutSaveProject(
            user1.id!!,
            snapshot.copy(files = modifiedFiles, entryFilePath = "app.py")
        )

        assertThat(res.entryFilePath).isEqualTo("app.py")
        assertThat(res.files.map { it.path }).containsExactly("app.py", "script-1.py")
    }

    @Test
    fun saveProject_duplicateNames_returnsError() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles[1].path = modifiedFiles[0].path

        assertErrorOnSave(user1.id!!, snapshot.copy(files = modifiedFiles), ErrorCode.DUPLICATE_FILE_NAME)
    }

    @Test
    fun saveProject_deleteEntryFile_returnsNoDeleteEntryFile() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        assertErrorOnSave(user1.id!!, snapshot.copy(files = snapshot.files.drop(1)), ErrorCode.NO_DELETE_ENTRY_FILE)
    }

    @Test
    fun saveProject_emptyRequest_returnsError() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        assertErrorOnSave(user1.id!!, snapshot.copy(files = emptyList()), ErrorCode.EMPTY_REQUEST)
    }

    @Test
    fun getProject_notOwnProject_returnsUnauthorized() {
        assertErrorOnGet(existingProject.id, user2.id!!, ErrorCode.NOT_ALLOWED)
    }

    @Test
    fun getProject_returnsProjectSnapshot() {
        val res = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        assertThat(res.projectId).isEqualTo(existingProject.id)
        assertThat(res.files).hasSize(2)
    }

    @Test
    fun duplicateProject_copiesAllFilesAndContent() {
        val originalSnapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val duplicatedId = submitPostDuplicateProject(existingProject.id, user1.id!!)

        val projects = submitGetUserProjects(user1.id!!)
        assertThat(projects.projects).hasSize(2)

        val duplicatedSnapshot = submitGetProjectSnapshot(duplicatedId, user1.id!!)
        assertThat(duplicatedSnapshot.projectId).isNotEqualTo(existingProject.id)
        assertThat(duplicatedSnapshot.projectName).isEqualTo(originalSnapshot.projectName)
        assertThat(duplicatedSnapshot.entryFilePath).isEqualTo(originalSnapshot.entryFilePath)
        assertThat(duplicatedSnapshot.files).hasSize(originalSnapshot.files.size)
        assertThat(duplicatedSnapshot.files.map { it.path })
            .containsExactlyInAnyOrderElementsOf(originalSnapshot.files.map { it.path })
        assertThat(duplicatedSnapshot.files.map { it.content })
            .containsExactlyInAnyOrderElementsOf(originalSnapshot.files.map { it.content })
    }

    @Test
    fun duplicateProject_modifyOriginalAfterDuplicate_duplicateUnaffected() {
        val duplicatedId = submitPostDuplicateProject(existingProject.id, user1.id!!)

        val originalSnapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val modifiedFiles = originalSnapshot.files.map { it.copy(content = "print('modified')") }
        submitPutSaveProject(user1.id!!, originalSnapshot.copy(files = modifiedFiles))

        val duplicatedSnapshot = submitGetProjectSnapshot(duplicatedId, user1.id!!)
        assertThat(duplicatedSnapshot.files.map { it.content })
            .containsExactlyInAnyOrder("print('hello world!')", "print('bye world!')")
    }

    @Test
    fun saveProject_updateContentOnly_pathsAndCountPreserved() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val originalPaths = snapshot.files.map { it.path }

        val updatedFiles = snapshot.files.map { it.copy(content = "# updated content") }
        val res = submitPutSaveProject(user1.id!!, snapshot.copy(files = updatedFiles))

        assertThat(res.files).hasSize(snapshot.files.size)
        assertThat(res.files.map { it.path }).containsExactlyElementsOf(originalPaths)
        assertThat(res.files.map { it.content }).containsOnly("# updated content")
    }

    @Test
    fun saveProject_notOwnProject_returnsError() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        assertErrorOnSave(user2.id!!, snapshot, ErrorCode.NOT_OWN_PROJECT)
    }

    @Test
    fun deleteProject_notOwnProject_returnsError() {
        assertErrorOnDelete(existingProject.id, user2.id!!, ErrorCode.NOT_OWN_PROJECT)

        val res = submitGetUserProjects(user1.id!!)
        assertThat(res.projects).hasSize(1)
    }

    @Test
    fun saveProject_consecutiveSaves_returnsLatestState() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)

        val firstSaveFiles = snapshot.files.map { it.copy(content = "print('version 1')") }
        submitPutSaveProject(user1.id!!, snapshot.copy(files = firstSaveFiles))

        val snapshotAfterFirst = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val secondSaveFiles = snapshotAfterFirst.files.map { it.copy(content = "print('version 2')") }
        val finalRes = submitPutSaveProject(user1.id!!, snapshotAfterFirst.copy(files = secondSaveFiles))

        assertThat(finalRes.files.map { it.content }).containsOnly("print('version 2')")
    }

    // --- edge cases ---

    @Test
    fun getProject_nonExistentProject_returnsNotFound() {
        assertErrorOnGet(UUID.randomUUID(), user1.id!!, ErrorCode.PROJECT_NOT_FOUND)
    }

    @Test
    fun saveProject_nonExistentProject_returnsNotFound() {
        val fakeSnapshot = ProjectSnapshot(
            projectId = UUID.randomUUID(),
            projectName = "Ghost Project",
            projectType = ProjectType.CODE,
            updatedAt = null,
            deleteAt = null,
            files = listOf(ProjectFileSnapshot(null, "script.py", pythonLanguage, "print('hello')")),
            entryFilePath = "script.py"
        )
        assertErrorOnSave(user1.id!!, fakeSnapshot, ErrorCode.PROJECT_NOT_FOUND)
    }

    @Test
    fun saveProject_entryFilePathNotInFiles_returnsError() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        assertErrorOnSave(user1.id!!, snapshot.copy(entryFilePath = "nonexistent.py"), ErrorCode.NO_DELETE_ENTRY_FILE)
    }

    @Test
    fun saveProject_fileContentExceedsLimit_returnsError() {
        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val oversizedFiles = snapshot.files.mapIndexed { i, file ->
            if (i == 0) file.copy(content = "x".repeat(512_001)) else file
        }
        assertErrorOnSave(user1.id!!, snapshot.copy(files = oversizedFiles), ErrorCode.FILE_TOO_LARGE)
    }

    @Test
    fun duplicateProject_otherUsersPrivateProject_returnsError() {
        // existingProject is PRIVATE by default
        assertErrorOnDuplicate(existingProject.id, user2.id!!, ErrorCode.NOT_OWN_PROJECT)
    }

    @Test
    fun duplicateProject_deleteOriginalAfterDuplicate_duplicateStillAccessible() {
        val duplicatedId = submitPostDuplicateProject(existingProject.id, user1.id!!)

        submitDeleteProject(existingProject.id, user1.id!!)

        assertErrorOnGet(existingProject.id, user1.id!!, ErrorCode.PROJECT_NOT_FOUND)

        val duplicatedSnapshot = submitGetProjectSnapshot(duplicatedId, user1.id!!)
        assertThat(duplicatedSnapshot.files).hasSize(2)
        assertThat(duplicatedSnapshot.files.map { it.content })
            .containsExactlyInAnyOrder("print('hello world!')", "print('bye world!')")
    }

    private fun newProjectRequest(name: String, filePath: String, language: String, content: String): CreateProjectRequest =
        CreateProjectRequest(
            projectName = name,
            projectType = ProjectType.CODE,
            files = listOf(
                ProjectFileSnapshot(
                    id = null,
                    path = filePath,
                    language = language,
                    content = content
                )
            ),
            entryFilePath = filePath,
            requestHash = UUID.randomUUID()
        )

    private fun submitGetProjectSnapshot(pid: UUID, userId: UUID): ProjectSnapshot =
        TestRestClient.getOk(ApiPaths.PROJECTS.byId(pid), userId, ProjectSnapshot::class.java)

    private fun submitPutSaveProject(userId: UUID, snapshot: ProjectSnapshot): ProjectSnapshot =
        TestRestClient.putOk(ApiPaths.PROJECTS.byId(snapshot.projectId), userId, snapshot, ProjectSnapshot::class.java)

    private fun submitGetUserProjects(userId: UUID): ProjectCardListResponse =
        TestRestClient.getOk(ApiPaths.PROJECTS.BASE, userId, ProjectCardListResponse::class.java)

    private fun submitDeleteProject(pid: UUID, userId: UUID) =
        TestRestClient.deleteNoContent(ApiPaths.PROJECTS.byId(pid), userId)

    private fun submitPatchRenameProject(request: RenameProjectRequest, userId: UUID) {
        TestRestClient.patchNoContent(ApiPaths.PROJECTS.name(request.targetId), userId, request)
    }

    private fun assertErrorOnGet(pid: UUID, userId: UUID, errorCode: ErrorCode): ValidatableResponse =
        TestRestClient.assertError("GET", ApiPaths.PROJECTS.byId(pid), userId, null, errorCode)

    private fun submitPostCreateProject(request: CreateProjectRequest, userId: UUID) =
        TestRestClient.postNoContent(ApiPaths.PROJECTS.BASE, userId, request)

    private fun assertErrorOnSave(userId: UUID, snapshot: ProjectSnapshot, errorCode: ErrorCode): ValidatableResponse =
        TestRestClient.assertError("PUT", ApiPaths.PROJECTS.byId(snapshot.projectId), userId, snapshot, errorCode)

    private fun submitPostDuplicateProject(pid: UUID, userId: UUID): UUID =
        TestRestClient.postOk(ApiPaths.PROJECTS.duplicateById(pid), userId, null, UUID::class.java)

    private fun assertErrorOnDuplicate(pid: UUID, userId: UUID, errorCode: ErrorCode): ValidatableResponse =
        TestRestClient.assertError("POST", ApiPaths.PROJECTS.duplicateById(pid), userId, null, errorCode)

    private fun assertErrorOnDelete(pid: UUID, userId: UUID, errorCode: ErrorCode): ValidatableResponse =
        TestRestClient.assertError("DELETE", ApiPaths.PROJECTS.byId(pid), userId, null, errorCode)

}