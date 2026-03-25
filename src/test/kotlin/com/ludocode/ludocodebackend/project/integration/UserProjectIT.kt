package com.ludocode.ludocodebackend.project.integration

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.util.sha256
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.projects.api.dto.request.RenameProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardListResponse
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.EnabledIf
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.Test

@EnabledIf(
    expression = "#{environment.getProperty('storage.mode') == 'gcs'}",
    loadContext = true
)
class UserProjectIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var languagesMapper: LanguagesMapper

    lateinit var existingProject: UserProject
    lateinit var existingFiles: List<ProjectFile>

    @BeforeEach
    fun seed() {

        val f1Id = UUID.randomUUID()
        val f2Id = UUID.randomUUID()

        existingProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = "Untitled",
                userId = user1.id!!,
                codeLanguage = pythonLanguage,
                createdAt = OffsetDateTime.now(clock).minusDays(2),
                updatedAt = OffsetDateTime.now(clock).minusDays(1),
                requestHash = UUID.randomUUID(),
                projectType = ProjectType.CODE,
                entryFilePath = null
            )
        )

        val projectId = existingProject.id

        val f1Path = "script.py"
        val f1Url = "$projectId/$f1Path"
        val f1Content = "print(hello world!)"
        val f2Path = "script-1.py"
        val f2Url = "$projectId/$f2Path"
        val f2Content = "print(bye world!)"

        existingFiles = projectFileRepository.saveAll(
            listOf(
                ProjectFile(
                    id = f1Id,
                    projectId = existingProject.id,
                    contentUrl = f1Url,
                    contentHash = sha256(f1Content),
                    filePath = f1Path,
                    codeLanguage = pythonLanguage
                ),
                ProjectFile(
                    id = f2Id,
                    projectId = existingProject.id,
                    contentUrl = f2Url,
                    contentHash = sha256(f2Content),
                    filePath = f2Path,
                    codeLanguage = pythonLanguage
                )
            )
        )

        existingProject.entryFilePath = f1Path
        existingProject = userProjectRepository.save(existingProject)

        try {
            storage.create(BucketInfo.of("lumo-file-content"))
        } catch (_: Exception) {
        }

        storage.list("lumo-file-content")
            .iterateAll()
            .forEach { blob -> storage.delete(blob.blobId) }

        storage.create(
            BlobInfo.newBuilder("lumo-file-content", f1Url).build(),
            f1Content.toByteArray()
        )
        storage.create(
            BlobInfo.newBuilder("lumo-file-content", f2Url).build(),
            f2Content.toByteArray()
        )
    }


    @Test
    fun createPythonProject_createsNew_returnsNewProjectsList() {

        val newProjectRequest = CreateProjectRequest(
            projectName = "Second Project",
            projectLanguageId = pythonLanguage.id,
            projectType = ProjectType.CODE,
            requestHash = UUID.randomUUID()
        )
        submitPostCreateProject(newProjectRequest, user1.id!!)
        val response = submitGetUserProjects(user1.id)
        assertThat(response).isNotNull()
        assertThat(response.projects.size).isEqualTo(2)
        assertThat(response.projects)
            .anyMatch { it.projectTitle == "Second Project" }



        val newProject = response.projects.find { it.projectTitle == "Second Project" }
        assertThat(newProject).isNotNull()

        val snapshotRes = submitGetProjectSnapshot(newProject!!.projectId, user1.id)

        assertThat(snapshotRes!!.files.size).isEqualTo(1)
        assertThat(snapshotRes.files[0].content).isEqualTo("print('Hello World!')")
        assertThat(snapshotRes.files[0].path).isEqualTo("script.py")

    }

    @Test
    fun createJsProject_createsNew_returnsNewProjectsList() {

        val newProjectRequest = CreateProjectRequest(
            projectName = "Third Project",
            projectLanguageId = jsLanguage.id,
            projectType = ProjectType.CODE,
            requestHash = UUID.randomUUID()
        )
         submitPostCreateProject(newProjectRequest, user1.id!!)
        val response = submitGetUserProjects(user1.id)

        assertThat(response).isNotNull()
        assertThat(response.projects.size).isEqualTo(2)
        assertThat(response.projects)
            .anyMatch { it.projectTitle == "Third Project" }

        val newProject = response.projects.find { it.projectTitle == "Third Project" }
        assertThat(newProject).isNotNull()

        val snapshotRes = submitGetProjectSnapshot(newProject!!.projectId, user1.id)
        assertThat(snapshotRes!!.files.size).isEqualTo(1)
        assertThat(snapshotRes.files[0].content).isEqualTo("console.log('Hello World!')")
        assertThat(snapshotRes.files[0].path).isEqualTo("script.js")

    }

    @Test
    fun deleteProject_deletesOnlyProject_returnsEmptyList() {

        val projectId = existingProject.id
        submitDeleteProject(projectId, user1.id!!)
        val res = submitGetUserProjects(user1.id)
        assertThat(res.projects).isEmpty()

    }

    @Test
    fun renameProject_renamesProject_returnsRenamed() {

        val projectId = existingProject.id
        val newName = "Test Project Name"
        val request = RenameProjectRequest(targetId = projectId, newName = newName)
        submitPatchRenameProject(request, user1.id!!)
        val res = submitGetUserProjects(user1.id)
        assertThat(res).isNotNull()
        assertThat(res.projects.size).isEqualTo(1)
        assertThat(res.projects[0].projectId).isEqualTo(projectId)
        assertThat(res.projects[0].projectTitle).isEqualTo(newName)

    }

    @Test
    fun renameProject_renamesProject_returnsRenamedFirst() {

        val newProjectRequest = CreateProjectRequest(
            projectName = "Second Project",
            projectLanguageId = pythonLanguage.id,
            projectType = ProjectType.CODE,
            requestHash = UUID.randomUUID()
        )
        submitPostCreateProject(newProjectRequest, user1.id!!)
        val response = submitGetUserProjects(userId = user1.id)

        val newProjectToModify = response.projects.find { it.projectTitle == "Second Project" }
        assertThat(newProjectToModify).isNotNull()
        val newProjectName = "Second Project Updated"

        val request = RenameProjectRequest(targetId = newProjectToModify!!.projectId, newProjectName)
        submitPatchRenameProject(request, user1.id!!)
        val res = submitGetUserProjects(user1.id)
        println(res.projects.joinToString("\n") { p ->
            "projectId=${p.projectId}, name=${p.projectTitle}"
        })
        assertThat(res).isNotNull()
        assertThat(res.projects.size).isEqualTo(2)
        assertThat(res.projects[0].projectId).isEqualTo(newProjectToModify.projectId)
        assertThat(res.projects[0].projectTitle).isEqualTo(newProjectName)


    }

    @Test
    fun createAndDelete_onlyCreatedRemains_returnsOnlyCreated() {

        val newProjectRequest = CreateProjectRequest(
            projectName = "Second Project",
            projectLanguageId = pythonLanguage.id,
            projectType = ProjectType.CODE,
            requestHash = UUID.randomUUID()
        )
        submitPostCreateProject(newProjectRequest, user1.id!!)
        val response = submitGetUserProjects(user1.id)
        assertThat(response).isNotNull()
        assertThat(response.projects.size).isEqualTo(2)
        assertThat(response.projects)
            .anyMatch { it.projectTitle == "Second Project" }

        val newProject = response.projects.find { it.projectTitle == "Second Project" }
        assertThat(newProject).isNotNull()

        val snapshot = submitGetProjectSnapshot(newProject!!.projectId, user1.id)
        assertThat(snapshot!!.files.size).isEqualTo(1)
        assertThat(snapshot.files[0].content).isEqualTo("print('Hello World!')")
        assertThat(snapshot.files[0].path).isEqualTo("script.py")

        val existingProjectId = existingProject.id
        submitDeleteProject(existingProjectId, user1.id!!)
        val deleteResponse = submitGetUserProjects(user1.id)
        assertThat(deleteResponse).isNotNull()
        assertThat(deleteResponse.projects.size).isEqualTo(1)
        assertThat(deleteResponse.projects[0].projectId).isEqualTo(newProject.projectId)

    }

    @Test
    fun saveProject_deleteAddAndRename_returnsSuccess() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        val languageMetadata = languagesMapper.toLanguageMetadata(pythonLanguage)

        println("Passed A")

        assertThat(snapshot).isNotNull()

        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles.removeAt(1)
        modifiedFiles.add(ProjectFileSnapshot(null, "script-2.py", languageMetadata, "print(2 + 2)"))
        modifiedFiles[0] = modifiedFiles[0].copy(content = "print('Awesome')")

        val snapshotCopy = snapshot.copy(files = modifiedFiles)

        val res = submitPutSaveProject(user1.id!!, snapshotCopy)
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

        val res = submitPutSaveProject(user1.id!!, snapshotCopy)

        assertThat(res).isNotNull()
        assertThat(res.files.size).isEqualTo(2)
        assertThat(res.files[0].path).isEqualTo(res.entryFilePath)
        assertThat(res.files.map { it.path }).containsExactly("script.py", "script-1.py")

    }

    @Test
    fun saveProject_renameEntryFileAndUpdateEntryFilePath_returnsSuccess() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()

        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles[0] = modifiedFiles[0].copy(path = "app.py")

        val snapshotCopy = snapshot.copy(
            files = modifiedFiles,
            entryFilePath = "app.py"
        )

        val res = submitPutSaveProject(user1.id!!, snapshotCopy)

        assertThat(res.entryFilePath).isEqualTo("app.py")
        assertThat(res.files[0].path).isEqualTo("app.py")
        assertThat(res.files.map { it.path }).containsExactly("app.py", "script-1.py")
    }

    @Test
    fun saveProject_fileIdFromAnotherProject_ignoresIncomingIdAndSaves() {
        val foreignProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = "Foreign Project",
                userId = user2.id!!,
                codeLanguage = pythonLanguage,
                createdAt = OffsetDateTime.now(clock),
                updatedAt = OffsetDateTime.now(clock),
                requestHash = UUID.randomUUID(),
                projectType = ProjectType.CODE,
                entryFilePath = null
            )
        )

        val foreignFile = projectFileRepository.save(
            ProjectFile(
                id = UUID.randomUUID(),
                projectId = foreignProject.id,
                contentUrl = "${foreignProject.id}/foreign.py",
                contentHash = sha256("print('foreign')"),
                filePath = "foreign.py",
                codeLanguage = pythonLanguage
            )
        )

        val snapshot = submitGetProjectSnapshot(existingProject.id, user1.id!!)
        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles[1] = modifiedFiles[1].copy(id = foreignFile.id)

        val snapshotCopy = snapshot.copy(files = modifiedFiles)

        val res = submitPutSaveProject(user1.id!!, snapshotCopy)
        assertThat(res.files.size).isEqualTo(2)
        assertThat(res.files.map { it.path }).containsExactly("script.py", "script-1.py")
    }

    @Test
    fun saveProject_duplicateNames_returnsError() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()
        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles[1].path = modifiedFiles[0].path
        val snapshotCopy = snapshot.copy(files = modifiedFiles)
        assertErrorOnSave(user1.id!!, snapshotCopy, ErrorCode.DUPLICATE_FILE_NAME)
    }

    @Test
    fun saveProject_deleteEntryFile_returnsNoDeleteEntryFile() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()

        val snapshotCopy = snapshot.copy(files = snapshot.files.drop(1))

        assertErrorOnSave(user1.id!!, snapshotCopy, ErrorCode.NO_DELETE_ENTRY_FILE)
    }

    @Test
    fun saveProject_emptyRequest_returnsError() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)
        assertThat(snapshot).isNotNull()
        val snapshotCopy = snapshot.copy(files = listOf())
        assertErrorOnSave(user1.id!!, snapshotCopy, ErrorCode.EMPTY_REQUEST)
    }

    @Test
    fun getProject_notOwnProject_returnsUnautharized() {
        val projectId = existingProject.id
        val userId = user2.id!!
        assertErrorOnGet(projectId, userId, ErrorCode.NOT_ALLOWED)
    }


    @Test
    fun getProject_returnsProjectSnapshot() {

        val projectId = existingProject.id

        val res = submitGetProjectSnapshot(projectId, user1.id!!)

        assertThat(res).isNotNull()

        assertThat(res.projectId).isEqualTo(projectId)
        assertThat(res.files.size).isEqualTo(2)

    }

    private fun submitGetProjectSnapshot(pid: UUID, userId: UUID): ProjectSnapshot =
        TestRestClient.getOk(ApiPaths.PROJECTS.byId(pid), userId, ProjectSnapshot::class.java)

    private fun submitPutSaveProject(userId: UUID, snapshot: ProjectSnapshot): ProjectSnapshot =
        TestRestClient.putOk(ApiPaths.PROJECTS.byId(snapshot.projectId), userId, snapshot, ProjectSnapshot::class.java)

    private fun submitGetUserProjects(userId: UUID): ProjectCardListResponse =
        TestRestClient.getOk(ApiPaths.PROJECTS.BASE, userId, ProjectCardListResponse::class.java)

    private fun submitDeleteProject(pid: UUID, userId: UUID) =
        TestRestClient.deleteNoContent(ApiPaths.PROJECTS.byId(pid), userId)

    private fun submitPatchRenameProject(request: RenameProjectRequest, userId: UUID) {
        TestRestClient.patchNoContent(
            ApiPaths.PROJECTS.name(request.targetId),
            userId,
            request,
        )
    }


    private fun assertErrorOnGet(pid: UUID, userId: UUID, errorCode: ErrorCode): ValidatableResponse? =
        TestRestClient.assertError("GET", ApiPaths.PROJECTS.byId(pid), userId, null, errorCode)

    private fun submitPostCreateProject(request: CreateProjectRequest, userId: UUID) =
        TestRestClient.postNoContent(ApiPaths.PROJECTS.BASE, userId, request)

    private fun assertErrorOnSave(userId: UUID, snapshot: ProjectSnapshot, errorCode: ErrorCode): ValidatableResponse? =
        TestRestClient.assertError("PUT", ApiPaths.PROJECTS.byId(snapshot.projectId), userId, snapshot, errorCode)


}