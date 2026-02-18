package com.ludocode.ludocodebackend.auth.integration.demo

import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.EnabledIf
import kotlin.test.Test

@EnabledIf(
    expression = "\${demo.enabled}",
    loadContext = true
)
class DemoAuthIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var demoConfig: DemoConfig

    @Test
    fun loginWithDemo_returnsDemoUser() {

        val id = demoConfig.userId ?: error("demo.user-id must be set for test")

        Assertions.assertThat(id == demoUser1.id)

        val res = submitGetDemoUser(demoToken)
        Assertions.assertThat(res).isNotNull()
        Assertions.assertThat(res.user.id).isEqualTo(id)
        Assertions.assertThat(res.user.displayName).isEqualTo(demoUser1.displayName)
        Assertions.assertThat(res.subscription.planCode).isEqualTo(Plan.FREE)
    }

    private fun submitGetDemoUser(token: String): UserLoginResponse {
        return RestAssured.given()
            .queryParam("token", token)
            .`when`()
            .get("${ApiPaths.AUTH.BASE}${ApiPaths.AUTH.DEMO}")
            .then()
            .statusCode(200)
            .extract()
            .`as`(UserLoginResponse::class.java)
    }

}