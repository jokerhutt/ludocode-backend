package com.ludocode.ludocodebackend.project.integration

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.commons.constants.PathConstants.CREATE_PROJECT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROJECT
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.util.sha256
import com.ludocode.ludocodebackend.playground.app.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.playground.app.dto.request.RenameRequest
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
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
    expression = "\${gcs.enabled}",
    loadContext = true
)
class UserProjectIT : AbstractIntegrationTest() {

    lateinit var existingProject: UserProject
    lateinit var existingFiles : List<ProjectFile>

    @BeforeEach
    fun seed () {

        existingProject = userProjectRepository.save(UserProject(
            id = UUID.randomUUID(),
            name = "Untitled",
            userId = user1.id!!,
            projectLanguage = LanguageType.python,
            createdAt = OffsetDateTime.now(clock).minusDays(2),
            updatedAt = OffsetDateTime.now(clock).minusDays(1),
            requestHash = UUID.randomUUID()
        ))

        val projectId = existingProject.id

        val f1Id = UUID.randomUUID()
        val f1Url = "$projectId/${f1Id}"
        val f1Content = "print(hello world!)"
        val f2Id = UUID.randomUUID()
        val f2Url ="$projectId/${f2Id}"
        val f2Content = "print(bye world!)"

        existingFiles = projectFileRepository.saveAll(listOf(
            ProjectFile(id = f1Id, projectId = existingProject.id, contentUrl = f1Url, contentHash = sha256(f1Content), filePath = "script.py", fileLanguage = LanguageType.python),
            ProjectFile(id = f2Id, projectId = existingProject.id, contentUrl = f2Url, contentHash = sha256(f2Content), filePath = "script-1.py", fileLanguage = LanguageType.python)
        ))

        try {
            storage.create(BucketInfo.of("ludo-file-content"))
        } catch (_: Exception) {}

        storage.list("ludo-file-content")
            .iterateAll()
            .forEach { blob -> storage.delete(blob.blobId) }

        storage.create(
            BlobInfo.newBuilder("ludo-file-content", f1Url).build(),
            f1Content.toByteArray()
        )
        storage.create(
            BlobInfo.newBuilder("ludo-file-content", f2Url).build(),
            f2Content.toByteArray()
        )
    }


    @Test
    fun createPythonProject_createsNew_returnsNewProjectsList() {

        val newProjectRequest = CreateProjectRequest(projectName = "Second Project", projectLanguage = LanguageType.python, requestHash = UUID.randomUUID())
        val response = submitPostCreateProject(newProjectRequest, user1.id!!)
        assertThat(response).isNotNull()
        assertThat(response.projects.size).isEqualTo(2)
        assertThat(response.projects)
            .anyMatch { it.projectName == "Second Project" }

        val newProject = response.projects.find { it.projectName == "Second Project" }
        assertThat(newProject).isNotNull()
        assertThat(newProject!!.files.size).isEqualTo(1)
        assertThat(newProject.files[0].content).isEqualTo("print('Hello World!')")
        assertThat(newProject.files[0].path).isEqualTo("script.py")

    }

    @Test
    fun createJsProject_createsNew_returnsNewProjectsList() {

        val newProjectRequest = CreateProjectRequest(projectName = "Third Project", projectLanguage = LanguageType.javascript, requestHash = UUID.randomUUID())
        val response = submitPostCreateProject(newProjectRequest, user1.id!!)
        assertThat(response).isNotNull()
        assertThat(response.projects.size).isEqualTo(2)
        assertThat(response.projects)
            .anyMatch { it.projectName == "Third Project" }

        val newProject = response.projects.find { it.projectName == "Third Project" }
        assertThat(newProject).isNotNull()
        assertThat(newProject!!.files.size).isEqualTo(1)
        assertThat(newProject.files[0].content).isEqualTo("console.log('Hello World!')")
        assertThat(newProject.files[0].path).isEqualTo("script.js")

    }

    @Test
    fun deleteProject_deletesOnlyProject_returnsEmptyList() {

        val projectId = existingProject.id
        val res = submitPostDeleteProject(projectId, user1.id!!)
        assertThat(res.projects).isEmpty()

    }

    @Test
    fun renameProject_renamesProject_returnsRenamed () {

        val projectId = existingProject.id
        val newName = "Test Project Name"
        val request = RenameRequest(targetId = projectId, newName = newName)
        val res = submitPostRenameProject(request, user1.id!!)
        assertThat(res).isNotNull()
        assertThat(res.projects.size).isEqualTo(1)
        assertThat(res.projects[0].projectId).isEqualTo(projectId)
        assertThat(res.projects[0].projectName).isEqualTo(newName)

    }

    @Test
    fun renameProject_renamesProject_returnsRenamedFirst () {

        val newProjectRequest = CreateProjectRequest(projectName = "Second Project", projectLanguage = LanguageType.python, requestHash = UUID.randomUUID())
        val response = submitPostCreateProject(newProjectRequest, user1.id!!)

        val newProjectToModify = response.projects.find { it.projectName == "Second Project" }
        assertThat(newProjectToModify).isNotNull()
        val newProjectName = "Second Project Updated"

        val request = RenameRequest(targetId = newProjectToModify!!.projectId, newProjectName)
        val res = submitPostRenameProject(request, user1.id!!)
        println(res.projects.joinToString("\n") { p ->
            "projectId=${p.projectId}, name=${p.projectName}"
        })
        assertThat(res).isNotNull()
        assertThat(res.projects.size).isEqualTo(2)
        assertThat(res.projects[0].projectId).isEqualTo(newProjectToModify.projectId)
        assertThat(res.projects[0].projectName).isEqualTo(newProjectName)


    }

    @Test
    fun createAndDelete_onlyCreatedRemains_returnsOnlyCreated() {

        val newProjectRequest = CreateProjectRequest(projectName = "Second Project", projectLanguage = LanguageType.python, requestHash = UUID.randomUUID())
        val response = submitPostCreateProject(newProjectRequest, user1.id!!)
        assertThat(response).isNotNull()
        assertThat(response.projects.size).isEqualTo(2)
        assertThat(response.projects)
            .anyMatch { it.projectName == "Second Project" }

        val newProject = response.projects.find { it.projectName == "Second Project" }
        assertThat(newProject).isNotNull()
        assertThat(newProject!!.files.size).isEqualTo(1)
        assertThat(newProject.files[0].content).isEqualTo("print('Hello World!')")
        assertThat(newProject.files[0].path).isEqualTo("script.py")

        val existingProjectId = existingProject.id
        val deleteResponse = submitPostDeleteProject(existingProjectId, user1.id!!)
        assertThat(deleteResponse).isNotNull()
        assertThat(deleteResponse.projects.size).isEqualTo(1)
        assertThat(deleteResponse.projects[0].projectId).isEqualTo(newProject.projectId)

    }

    @Test
    fun saveProject_deleteAddAndRename_returnsSuccess() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)

        println("Passed A")

        assertThat(snapshot).isNotNull()

        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles.removeAt(1)
        modifiedFiles.add(ProjectFileSnapshot(null, "script-2.py", LanguageType.python, "print(2 + 2)"))
        modifiedFiles[0] = modifiedFiles[0].copy(content = "print('Awesome')")

        val snapshotCopy = snapshot.copy(files = modifiedFiles)

        val res = submitPostSaveProjectSnapshot(user1.id!!, snapshotCopy)
        assertThat(res).isNotNull()
        assertThat(res.projectId).isEqualTo(projectId)

        assertThat(res.files.size).isEqualTo(2)
        assertThat(res.files[0].path).isEqualTo(snapshotCopy.files[0].path)
        assertThat(res.files[0].content).isEqualTo(snapshotCopy.files[0].content)
        assertThat(res.files[1].path).isEqualTo("script-2.py")
        assertThat(res.files[1].content).isEqualTo("print(2 + 2)")
    }

    @Test
    fun saveProject_swapNames_returnsSuccess() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()
        val modifiedFiles = snapshot.files.toMutableList()

        val fileOneName = modifiedFiles[0].copy().path
        modifiedFiles[0].path = modifiedFiles[1].path
        modifiedFiles[1].path = fileOneName

        val snapshotCopy = snapshot.copy(files = modifiedFiles)

        val res = submitPostSaveProjectSnapshot(user1.id!!, snapshotCopy)

        assertThat(res).isNotNull()
        assertThat(res.files.size).isEqualTo(2)

        assertThat(res.files[0].path).isEqualTo(snapshotCopy.files[0].path)
        assertThat(res.files[1].path).isEqualTo(snapshotCopy.files[1].path)

    }

    @Test
    fun saveProject_duplicateNames_returnsError () {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()
        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles[1].path = modifiedFiles[0].path
        val snapshotCopy = snapshot.copy(files = modifiedFiles)
        assertErrorOnSave(user1.id!!, snapshotCopy, ErrorCode.DUPLICATE_FILE_NAME)
    }

    @Test
    fun saveProject_emptyRequest_returnsError () {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()
        val snapshotCopy = snapshot.copy(files = listOf())
        assertErrorOnSave(user1.id!!, snapshotCopy, ErrorCode.EMPTY_REQUEST)
    }

    @Test
    fun saveProject_invalidFileName_returnsError () {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()
        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles[1].path = "script1py"
        val snapshotCopy = snapshot.copy(files = modifiedFiles)
        assertErrorOnSave(user1.id!!, snapshotCopy, ErrorCode.INVALID_FILE_NAME)
    }

    @Test
    fun getProject_notOwnProject_returnsUnautharized () {
        val projectId = existingProject.id
        val userId = user2.id!!
        assertErrorOnGet(projectId, userId, ErrorCode.NOT_ALLOWED)
    }


    @Test
    fun getProject_returnsProjectSnapshot () {

        val projectId = existingProject.id

       val res = submitGetProjectSnapshot(projectId, user1.id!!)

       assertThat(res).isNotNull()

       assertThat(res.projectId).isEqualTo(projectId)
       assertThat(res.files.size).isEqualTo(2)

    }

    private fun submitGetProjectSnapshot (pid: UUID, userId: UUID): ProjectSnapshot =
        TestRestClient.getOk("$PROJECT/$pid/get", userId, ProjectSnapshot::class.java)

    private fun submitPostSaveProjectSnapshot (userId: UUID, snapshot: ProjectSnapshot): ProjectSnapshot =
        TestRestClient.postOk("$PROJECT/save", userId, snapshot, ProjectSnapshot::class.java)

    private fun submitPostDeleteProject (pid: UUID, userId: UUID) : ProjectListResponse =
        TestRestClient.postOk("$PROJECT/$pid/delete", userId, null, ProjectListResponse::class.java)

    private fun submitPostRenameProject (request: RenameRequest, userId: UUID) : ProjectListResponse =
        TestRestClient.postOk("$PROJECT/rename", userId, request, ProjectListResponse::class.java)

    private fun submitPostCreateProject (request: CreateProjectRequest, userId: UUID) : ProjectListResponse =
        TestRestClient.postOk("$PROJECT$CREATE_PROJECT", userId, request, ProjectListResponse::class.java)

    private fun assertErrorOnGet (pid: UUID, userId: UUID, errorCode: ErrorCode): ValidatableResponse? =
        TestRestClient.assertError("GET", "$PROJECT/$pid/get", userId, null, errorCode)

    private fun assertErrorOnSave (userId: UUID, snapshot: ProjectSnapshot, errorCode: ErrorCode): ValidatableResponse? =
        TestRestClient.assertError("POST", "$PROJECT/save", userId, snapshot, errorCode)



}