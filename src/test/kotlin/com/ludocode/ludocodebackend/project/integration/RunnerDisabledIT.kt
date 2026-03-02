package com.ludocode.ludocodebackend.project.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.DisabledIf
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.Test

@DisabledIf(
    expression = "\${piston.enabled}",
    loadContext = true
)
class RunnerDisabledIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var languagesMapper: LanguagesMapper

    @Test
    fun runCode_returns403_whenFeatureDisabled() {

        val languageMetadata = languagesMapper.toLanguageMetadata(pythonLanguage)
        val testRequest = ProjectSnapshot(
            projectId = UUID.randomUUID(),
            projectName = "I Wont run",
            updatedAt = OffsetDateTime.now(clock),
            projectLanguage = languageMetadata,
            files = listOf(),
            deleteAt = null,
            entryFileId = UUID.randomUUID()
        )

        given()
            .header("X-Test-User-Id", user1.id.toString())
            .contentType(ContentType.JSON)
            .body(testRequest)
            .`when`().post("${ApiPaths.RUNNER.BASE}${ApiPaths.RUNNER.EXECUTE}")
            .then()
            .statusCode(403)

    }


}