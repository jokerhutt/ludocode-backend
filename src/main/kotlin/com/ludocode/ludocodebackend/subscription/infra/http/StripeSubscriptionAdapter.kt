package com.ludocode.ludocodebackend.subscription.infra.http

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.snapshot.StripeSubscriptionSnapshot
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionPort
import com.stripe.model.Subscription
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class StripeSubscriptionAdapter : StripeSubscriptionPort {

    override fun retrieveSnapshot(subscriptionId: String): StripeSubscriptionSnapshot {

        val stripeSub = Subscription.retrieve(subscriptionId)

        val item = stripeSub.items.data.firstOrNull()
            ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID)

        return StripeSubscriptionSnapshot(
            priceId = item.price?.id
                ?: throw ApiException(ErrorCode.STRIPE_SUBSCRIPTION_INVALID),
            customerId = stripeSub.customer
                ?: throw ApiException(ErrorCode.STRIPE_CUSTOMER_INVALID),
            subscriptionId = stripeSub.id,
            periodStart = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(item.currentPeriodStart),
                ZoneOffset.UTC
            ),
            periodEnd = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(item.currentPeriodEnd),
                ZoneOffset.UTC
            )
        )
    }
}