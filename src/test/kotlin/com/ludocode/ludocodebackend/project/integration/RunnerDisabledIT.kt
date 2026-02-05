package com.ludocode.ludocodebackend.project.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.playground.api.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.springframework.test.context.junit.jupiter.DisabledIf
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.test.Test

@DisabledIf(
    expression = "\${piston.enabled}",
    loadContext = true
)
class RunnerDisabledIT : AbstractIntegrationTest() {

    @Test
    fun runCode_returns403_whenFeatureDisabled() {

        val testRequest = ProjectSnapshot(projectId = UUID.randomUUID(), projectName = "I Wont run", updatedAt = OffsetDateTime.now(clock), projectLanguage = pythonLanguage, files = listOf())

        given()
            .header("X-Test-User-Id", user1.id.toString())
            .contentType(ContentType.JSON)
            .body(testRequest)
            .`when`().post("${ApiPaths.RUNNER.BASE}${ApiPaths.RUNNER.EXECUTE}")
            .then()
            .statusCode(403)

    }


}