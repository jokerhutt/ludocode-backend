package com.ludocode.ludocodebackend.progress.unit

import com.ludocode.ludocodebackend.commons.constants.PathConstants.CREATE_PROJECT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROJECT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.RUN_PROJECT
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonRequest
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonResponse
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonRun
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectSnapshot
import com.ludocode.ludocodebackend.playground.app.dto.response.ProjectListResponse
import com.ludocode.ludocodebackend.playground.app.dto.response.RunnerResult
import com.ludocode.ludocodebackend.playground.app.port.out.PistonOutboundPort
import com.ludocode.ludocodebackend.playground.app.service.CodeRunnerService
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID
import kotlin.test.Test

class CodeRunnerServiceTest : AbstractIntegrationTest() {

    @Test
    fun `uses correct runtime and maps response`() {

        val files = listOf(
            ProjectFileSnapshot(
                id = null,
                path = "script.py",
                language = LanguageType.python,
                content = "print('hi')",
            )
        )

        val project = makeDummyProject(files)

        val result = submitRunCode(project, user1.id!!)

        assertThat(result).isNotNull()
        println(result.stdout)
        assertThat(result.stdout).isEqualTo("hi\n")
        assertThat(result.stderr).isEqualTo("")
        assertThat(result.exitCode).isEqualTo(0)

    }

    private fun makeDummyProject (files: List<ProjectFileSnapshot>): ProjectSnapshot {
        return ProjectSnapshot (
            projectId = UUID.randomUUID(),
            projectName = "My Project",
            projectLanguage = LanguageType.python,
            files = files

        )
    }

    fun submitRunCode (request: ProjectSnapshot, userId: UUID) : RunnerResult {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .contentType(io.restassured.http.ContentType.JSON)
            .body(request)
            .`when`()
            .post("$PROJECT$RUN_PROJECT")
            .then()
            .statusCode(200)
            .extract()
            .`as`(RunnerResult::class.java)
    }


}