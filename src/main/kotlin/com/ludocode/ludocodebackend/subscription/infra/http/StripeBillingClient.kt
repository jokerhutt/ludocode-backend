package com.ludocode.ludocodebackend.subscription.infra.http
import com.ludocode.ludocodebackend.commons.configuration.app.AppProperties
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeBillingPort
import com.stripe.model.billingportal.Session
import com.stripe.param.billingportal.SessionCreateParams
import org.slf4j.LoggerFactory

class StripeBillingClient(
    private val appProperties: AppProperties
) : StripeBillingPort {

    private val logger = LoggerFactory.getLogger(StripeBillingClient::class.java)

    override fun createBillingPortalSession(customerId: String): String {

        val params = SessionCreateParams.builder()
            .setCustomer(customerId)
            .setReturnUrl("${appProperties.frontendUrl}/courses")
            .build()

        val session = Session.create(params)

        return session.url
    }

}