package com.ludocode.ludocodebackend.auth.integration.demo

import com.ludocode.ludocodebackend.auth.api.dto.response.UserLoginResponse
import com.ludocode.ludocodebackend.auth.config.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.EnabledIf
import kotlin.test.Test

@EnabledIf(
    expression = "#{ '\${demo.enabled:false}' == 'false' }",
    reason = "Runs only when demo mode is disabled"
)
class DemoAuthDisabledIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var demoConfig: DemoConfig

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