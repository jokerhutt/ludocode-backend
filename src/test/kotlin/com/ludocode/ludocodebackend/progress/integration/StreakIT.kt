package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.commons.constants.PathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID
import kotlin.test.Test

class StreakIT : AbstractIntegrationTest() {

    @Test
    fun submitGetStreak_noStreakYet_returnsNew () {

        val userId = user1.id

        val res = submitGetStreak(userId!!)

        assertThat(res).isNotNull()
        assertThat(res.current).isEqualTo(0)
        assertThat(res.best).isEqualTo(0)
        assertThat(res.lastMet).isNull()

    }

    private fun submitGetStreak (userId: UUID): UserStreakResponse {
        return given()
            .header("X-Test-User-Id", userId.toString())
            .`when`()
            .get("${PathConstants.STREAK}/get")
            .then()
            .statusCode(200)
            .extract()
            .`as`(UserStreakResponse::class.java)
    }
}