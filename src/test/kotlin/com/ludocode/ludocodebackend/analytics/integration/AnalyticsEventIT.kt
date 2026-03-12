package com.ludocode.ludocodebackend.analytics.integration

import com.ludocode.ludocodebackend.analytics.api.dto.AnalyticsEventRequest
import com.ludocode.ludocodebackend.analytics.domain.enums.AnalyticsEventKey
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class AnalyticsEventIT : AbstractIntegrationTest() {

    @Test
    fun sendsSignupClickAnalytic_savesToRepository () {

        val req = AnalyticsEventRequest(
            event = AnalyticsEventKey.SIGNUP_CLICK,
            properties = mapOf(
                "source" to "landing"
            )
        )

        submitPostSubmitAnalytics(req)

        analyticsEventRepository.flush()

        val analyticsEvents = analyticsEventRepository.findAll()

        assertThat(analyticsEvents).isNotEmpty()
        assertThat(analyticsEvents.size).isEqualTo(1)

        val submittedEvent = analyticsEvents[0]
        assertThat(submittedEvent).isNotNull()

        assertThat(submittedEvent.event).isEqualTo(req.event)
        assertThat(submittedEvent.properties).isEqualTo(req.properties)


    }


    fun submitPostSubmitAnalytics (req: AnalyticsEventRequest) =
        TestRestClient.postNoContent(ApiPaths.ANALYTICS.BASE, user1.id, req)



}