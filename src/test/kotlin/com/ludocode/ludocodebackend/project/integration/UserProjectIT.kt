package com.ludocode.ludocodebackend.project.integration

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROGRESS_COURSE
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROJECT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SAVE_PROJECT
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

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
            createdAt = OffsetDateTime.now(clock).minusDays(1),
            updatedAt = OffsetDateTime.now(clock)
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
    fun saveProject_deleteAddAndRename_returnsSuccess() {
        val projectId = existingProject.id
        val snapshot = submitGetProjectSnapshot(projectId, user1.id!!)

        println("Passed A")

        assertThat(snapshot).isNotNull()

        val modifiedFiles = snapshot.files.toMutableList()
        modifiedFiles.removeAt(1)
        modifiedFiles[0] = modifiedFiles[0].copy(content = "print('Awesome')")

        val snapshotCopy = snapshot.copy(files = modifiedFiles)

        val res = submitPostSaveProjectSnapshot(projectId, user1.id!!, snapshotCopy)
        assertThat(res).isNotNull()
        assertThat(res.projectId).isEqualTo(projectId)

        assertThat(res.files.size).isEqualTo(1)
        assertThat(res.files[0].path).isEqualTo(snapshotCopy.files[0].path)
        assertThat(res.files[0].content).isEqualTo(snapshotCopy.files[0].content)
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

        val res = submitPostSaveProjectSnapshot(projectId, user1.id!!, snapshotCopy)

        assertThat(res).isNotNull()
        assertThat(res.files.size).isEqualTo(2)

        assertThat(res.files[0].path).isEqualTo(snapshotCopy.files[0].path)
        assertThat(res.files[1].path).isEqualTo(snapshotCopy.files[1].path)


    }

    @Test
    fun getProject_returnsProjectSnapshot () {

        val projectId = existingProject.id

       val res = submitGetProjectSnapshot(projectId, user1.id!!)

       assertThat(res).isNotNull()

       assertThat(res.projectId).isEqualTo(projectId)
       assertThat(res.files.size).isEqualTo(2)

    }

    private fun submitGetProjectSnapshot (pid: UUID, userId: UUID): ProjectSnapshot {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get("$PROJECT/$pid/get")
            .then()
            .statusCode(200)
            .extract()
            .`as`(ProjectSnapshot::class.java)
    }

    private fun submitPostSaveProjectSnapshot (pid: UUID, userId: UUID, snapshot: ProjectSnapshot): ProjectSnapshot {
       return given()
           .header("X-Test-User-Id", userId.toString())
           .contentType(io.restassured.http.ContentType.JSON)
           .body(snapshot)
           .`when`()
           .post("$PROJECT/$pid/save")
           .then()
           .statusCode(200)
           .extract()
           .`as`(ProjectSnapshot::class.java)
    }




    fun sha256(text: String): String {
        val bytes = MessageDigest
            .getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

}