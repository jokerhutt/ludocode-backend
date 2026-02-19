package com.ludocode.ludocodebackend.auth.integration.demo

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.springframework.test.context.junit.jupiter.DisabledIf
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
            .get("${ApiPaths.AUTH.BASE}${ApiPaths.AUTH.DEMO}")
            .then()
            .statusCode(403)
    }


}