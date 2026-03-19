package com.ludocode.ludocodebackend.project.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.projects.api.dto.response.ProjectLikeCountResponse
import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
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
class ProjectLikeIT : AbstractIntegrationTest() {

    private lateinit var publicProject: UserProject
    private lateinit var privateProject: UserProject

    @BeforeEach
    fun seedProjects() {
        val now = OffsetDateTime.now(clock)

        publicProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = "Public project",
                userId = user1.id,
                requestHash = UUID.randomUUID(),
                codeLanguage = pythonLanguage,
                projectVisibility = Visibility.PUBLIC,
                createdAt = now,
                updatedAt = now,
            )
        )

        privateProject = userProjectRepository.save(
            UserProject(
                id = UUID.randomUUID(),
                name = "Private project",
                userId = user1.id,
                requestHash = UUID.randomUUID(),
                codeLanguage = pythonLanguage,
                projectVisibility = Visibility.PRIVATE,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    @Test
    fun likeProject_publicProject_returnsCountForRequestedIds() {
        like(publicProject.id, user2.id)

        val counts = getLikeCounts(user2.id, listOf(publicProject.id, privateProject.id))

        assertThat(counts).hasSize(2)
        assertThat(counts[0].id).isEqualTo(publicProject.id)
        assertThat(counts[0].count).isEqualTo(1)
        assertThat(counts[1].id).isEqualTo(privateProject.id)
        assertThat(counts[1].count).isEqualTo(0)
    }

    @Test
    fun likeProject_sameUserTwice_isIdempotent() {
        like(publicProject.id, user2.id)
        like(publicProject.id, user2.id)

        val counts = getLikeCounts(user2.id, listOf(publicProject.id))

        assertThat(counts).hasSize(1)
        assertThat(counts[0].id).isEqualTo(publicProject.id)
        assertThat(counts[0].count).isEqualTo(1)
    }

    @Test
    fun unlikeProject_existingLike_removesLike() {
        like(publicProject.id, user2.id)

        TestRestClient.deleteNoContent(ApiPaths.PROJECTS.likeById(publicProject.id), user2.id)

        val counts = getLikeCounts(user2.id, listOf(publicProject.id))

        assertThat(counts).hasSize(1)
        assertThat(counts[0].count).isEqualTo(0)
    }

    @Test
    fun likeProject_privateProjectByNonOwner_returnsNotAllowed() {
        TestRestClient.assertError(
            method = "POST",
            url = ApiPaths.PROJECTS.likeById(privateProject.id),
            userId = user2.id,
            expected = ErrorCode.NOT_ALLOWED,
        )
    }

    private fun like(projectId: UUID, userId: UUID) {
        given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .`when`()
            .post(ApiPaths.PROJECTS.likeById(projectId))
            .then()
            .statusCode(204)
    }

    private fun getLikeCounts(userId: UUID, projectIds: List<UUID>): List<ProjectLikeCountResponse> {
        val response = TestRestClient.getOk(
            url = ApiPaths.PROJECTS.BASE + ApiPaths.PROJECTS.LIKE,
            userId = userId,
            responseType = Array<ProjectLikeCountResponse>::class.java,
            queryParams = mapOf("projectIds" to projectIds),
        )
        return response.toList()
    }
}


