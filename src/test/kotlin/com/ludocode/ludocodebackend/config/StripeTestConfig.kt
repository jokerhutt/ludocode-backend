package com.ludocode.ludocodebackend.config

import com.ludocode.ludocodebackend.subscription.api.dto.snapshot.StripeSubscriptionSnapshot
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionCommandPort
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionPort
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.OffsetDateTime
import java.util.UUID

@TestConfiguration
class StripeTestConfig {

    @Bean
    fun stripeSubscriptionPort(): StripeSubscriptionPort =
        object : StripeSubscriptionPort {
            override fun retrieveSnapshot(subscriptionId: String): StripeSubscriptionSnapshot {
                return StripeSubscriptionSnapshot(
                    priceId = "price_supporter",
                    customerId = "cus_test",
                    subscriptionId = subscriptionId,
                    periodStart = OffsetDateTime.now().minusDays(1),
                    periodEnd = OffsetDateTime.now().plusMonths(1),
                    status = "active"
                )
            }
        }

    @Bean
    fun stripeSubscriptionCommandPort(): StripeSubscriptionCommandPort =
        object : StripeSubscriptionCommandPort {
            override fun cancelSubscription(subscriptionId: String) {}
            override fun createCustomer(email: String, name: String?) = "cus_new_test"
            override fun createSubscription(customerId: String, priceId: String, userId: UUID) = "sub_test"
        }
}