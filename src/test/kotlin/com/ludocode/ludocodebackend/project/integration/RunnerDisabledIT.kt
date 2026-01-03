package com.ludocode.ludocodebackend.project.integration

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.springframework.test.context.junit.jupiter.DisabledIf
import java.time.LocalDate
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

        val testRequest = ProjectSnapshot(projectId = UUID.randomUUID(), projectName = "I Wont run", updatedAt = OffsetDateTime.now(clock), projectLanguage = LanguageType.python, files = listOf())

        given()
            .header("X-Test-User-Id", user1.id.toString())
            .contentType(ContentType.JSON)
            .body(testRequest)
            .`when`().post("${PathConstants.RUNNER}${PathConstants.RUN_PROJECT}")
            .then()
            .statusCode(403)

    }


}