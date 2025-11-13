package com.ludocode.ludocodebackend.project.integration

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class UserProjectIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun getProject_returnsProjectSnapshot () {

       val existingProject = userProjectRepository.save(UserProject(
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

       val existingFiles = projectFileRepository.saveAll(listOf(
           ProjectFile(id = f1Id, projectId = existingProject.id, contentUrl = f1Url, contentHash = sha256(f1Content), filePath = "script.py", fileLanguage = LanguageType.python),
           ProjectFile(id = f2Id, projectId = existingProject.id, contentUrl = f2Url, contentHash = sha256(f2Content), filePath = "script-1.py", fileLanguage = LanguageType.python)
       ))

        try {
            storage.create(BucketInfo.of("ludo-file-content"))
        } catch (_: Exception) { /* ignore if exists */ }

        storage.create(
            BlobInfo.newBuilder("ludo-file-content", f1Url).build(),
            f1Content.toByteArray()
        )

        storage.create(
            BlobInfo.newBuilder("ludo-file-content", f2Url).build(),
            f2Content.toByteArray()
        )

       val res = submitGetProjectSnapshot(projectId, user1.id!!)

       assertThat(res).isNotNull()

       assertThat(res.projectId).isEqualTo(projectId)
       assertThat(res.files.size).isEqualTo(2)




    }

    private fun submitGetProjectSnapshot (pid: UUID, userId: UUID): ProjectSnapshot {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get("${PathConstants.PROJECT}/$pid/get")
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