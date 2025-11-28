package com.ludocode.ludocodebackend.auth.integration.demo

import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Disabled
import org.springframework.test.context.junit.jupiter.DisabledIf
import org.springframework.test.context.junit.jupiter.EnabledIf
import kotlin.test.Test

@DisabledIf(
    expression = "\${demo.enabled}",
    loadContext = true
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