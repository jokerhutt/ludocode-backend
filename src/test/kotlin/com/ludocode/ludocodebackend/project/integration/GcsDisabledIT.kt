package com.ludocode.ludocodebackend.project.integration
import com.ludocode.ludocodebackend.commons.constants.PathConstants.CREATE_PROJECT
import com.ludocode.ludocodebackend.commons.constants.PathConstants.PROJECT
import com.ludocode.ludocodebackend.playground.app.dto.request.CreateProjectRequest
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.springframework.test.context.junit.jupiter.EnabledIf
import java.util.UUID
import kotlin.test.Test

@EnabledIf(
    expression = "#{ '\${gcs.enabled:false}' == 'false' }",
    reason = "Runs only when gcs is disabled and tests filter chain. To enable GCS & projects set gcs.enabled = true in application.yaml"
)
class GcsDisabledIT : AbstractIntegrationTest() {

    @Test
    fun createProject_returns403_whenFeatureDisabled() {

        val uid = user1.id!!
        val newProjectRequest = CreateProjectRequest(projectName = "Test Project", projectLanguage = LanguageType.python, requestHash = UUID.randomUUID())

        given()
            .header("X-Test-User-Id", uid.toString())
            .contentType(ContentType.JSON)
            .body(newProjectRequest)
            .`when`().post("$PROJECT$CREATE_PROJECT")
            .then()
            .statusCode(403)

    }

    @Test
    fun getProject_returns403_whenFeatureDisabled() {

        val dummyRequestId = UUID.randomUUID()
        given()
            .header("X-Test-User-Id", user1.id.toString())
            .contentType(ContentType.JSON)
            .`when`().get("$PROJECT/$dummyRequestId/save")
            .then()
            .statusCode(403)

    }

}