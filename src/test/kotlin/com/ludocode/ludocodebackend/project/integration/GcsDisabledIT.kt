package com.ludocode.ludocodebackend.project.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.projects.api.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.springframework.test.context.junit.jupiter.DisabledIf
import java.util.*
import kotlin.test.Test

@DisabledIf(
    expression = "#{environment.getProperty('storage.mode') == 'gcs'}",
    loadContext = true
)
class GcsDisabledIT : AbstractIntegrationTest() {

    @Test
    fun createProject_returns403_whenFeatureDisabled() {

        val uid = user1.id!!
        val newProjectRequest = CreateProjectRequest(
            projectName = "Test Project",
            projectType = ProjectType.CODE,
            files = listOf(
                ProjectFileSnapshot(
                    id = null,
                    path = "script.py",
                    language = pythonLanguage,
                    content = "print('Hello World!')"
                )
            ),
            entryFilePath = "script.py",
            requestHash = UUID.randomUUID()
        )

        given()
            .header("X-Test-User-Id", uid.toString())
            .contentType(ContentType.JSON)
            .body(newProjectRequest)
            .`when`().post(ApiPaths.PROJECTS.BASE)
            .then()
            .statusCode(403)

    }

    @Test
    fun getProject_returns403_whenFeatureDisabled() {

        val dummyRequestId = UUID.randomUUID()
        given()
            .header("X-Test-User-Id", user1.id.toString())
            .contentType(ContentType.JSON)
            .`when`().get("${ApiPaths.PROJECTS.BASE}/$dummyRequestId")
            .then()
            .statusCode(403)

    }

}