package com.ludocode.ludocodebackend.auth.integration.demo

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.springframework.test.context.junit.jupiter.EnabledIf
import kotlin.test.Test

@EnabledIf(
    expression = "#{ '\${demo.enabled:false}' == 'false' }",
    reason = "Runs only when demo mode is disabled"
)
class DemoAuthDisabledIT : AbstractIntegrationTest() {

    @Test
    fun demoLogin_returns403_whenFeatureDisabled() {
        given()
            .queryParam("token", "anything")
            .`when`()
            .get("${PathConstants.AUTH}${PathConstants.DEMO_LOGIN}")
            .then()
            .statusCode(403)
    }


}