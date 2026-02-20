package com.ludocode.ludocodebackend.subscription.infra.http

import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionCommandPort
import com.stripe.model.Subscription
import org.springframework.stereotype.Component

@Component
class StripeSubscriptionCommandAdapter : StripeSubscriptionCommandPort {

    override fun cancelSubscription(subscriptionId: String) {
        val stripeSub = Subscription.retrieve(subscriptionId)
        stripeSub.cancel()
    }
}