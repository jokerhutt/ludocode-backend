package com.ludocode.ludocodebackend.project.integration

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.util.sha256
import com.ludocode.ludocodebackend.projects.api.dto.request.ChangeVisibilityRequest
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectCardListResponse
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

class ProjectVisibilityIT : AbstractIntegrationTest() {

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
                    filePath = f1Path,
                    codeLanguage = pythonLanguage
                ),
                ProjectFile(
                    id = f2Id,
                    projectId = existingProject.id,
                    contentUrl = f2Url,
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
    fun setOtherUsersProjectVisibility_throwsNotAllowed() {
        assertErrorOnPutVisibility(user2.id, existingProject.id, ChangeVisibilityRequest(Visibility.PUBLIC), ErrorCode.NOT_OWN_PROJECT)
    }

    @Test
    fun setProjectToPrivate_setsToPrivate() {

        val currentProjectsRes = submitGetUserProjects(user1.id)
        val currentProjects = currentProjectsRes.projects

        val project = currentProjects.find { it.projectId == existingProject.id }
            ?: fail("Expected project not found")

        assertThat(project.visibility).isEqualTo(Visibility.PRIVATE)

        submitPutChangeProjectVisibility(user1.id, existingProject.id, ChangeVisibilityRequest(Visibility.PUBLIC))

        val res = submitGetUserProjects(user1.id)

        val updatedProject = res.projects.find { it.projectId == existingProject.id }
            ?: fail("Expected project not found")

        assertThat(updatedProject.visibility).isEqualTo(Visibility.PUBLIC)

    }

    private fun submitPutChangeProjectVisibility(userId: UUID, projectId: UUID, req: ChangeVisibilityRequest) =
        TestRestClient.putNoContent(ApiPaths.PROJECTS.visibilityById(projectId), userId, req)

    private fun assertErrorOnPutVisibility(userId: UUID, projectId: UUID, req: ChangeVisibilityRequest, errorCode: ErrorCode) : ValidatableResponse? =
        TestRestClient.assertError("PUT", ApiPaths.PROJECTS.visibilityById(projectId), userId, req, errorCode)

    private fun submitGetUserProjects(userId: UUID): ProjectCardListResponse =
        TestRestClient.getOk(ApiPaths.PROJECTS.BASE, userId, ProjectCardListResponse::class.java)


}