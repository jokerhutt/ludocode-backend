package com.ludocode.ludocodebackend.auth.integration.demo

import com.ludocode.ludocodebackend.auth.configuration.demo.DemoProperties
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

class DemoAuthIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var demoProperties: DemoProperties

    @Test
    fun getMe_returnsDemoUser_whenFirebaseDisabled() {
        val response = given()
            .`when`()
            .get("${ApiPaths.AUTH.BASE}${ApiPaths.AUTH.ME}")
            .then()
            .statusCode(200)
            .extract()
            .`as`(UserResponse::class.java)

        assertThat(response).isNotNull()
        assertThat(response.id).isEqualTo(demoProperties.userId)
        assertThat(response.displayName).isEqualTo("Demo User")
    }
}