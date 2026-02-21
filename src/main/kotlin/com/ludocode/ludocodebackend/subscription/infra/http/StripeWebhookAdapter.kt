package com.ludocode.ludocodebackend.subscription.infra.http

import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionPort
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeWebhookPort
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.subscription.configuration.StripeProperties
import com.stripe.model.Invoice
import com.stripe.model.Subscription
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import org.springframework.stereotype.Component
import java.util.UUID


@Component
class StripeWebhookAdapter(
    private val stripeProperties: StripeProperties,
    private val subscriptionService: SubscriptionService,
    private val stripeSubscriptionPort: StripeSubscriptionPort
) : StripeWebhookPort {

    override fun handle(payload: String, signature: String) {

        val event = Webhook.constructEvent(
            payload,
            signature,
            stripeProperties.webhookSecret
        )

        val sub = event.dataObjectDeserializer
            .getObject()
            .orElse(null) as? Subscription
            ?: return

        val snapshot = stripeSubscriptionPort.retrieveSnapshot(sub.id)

        when (event.type) {

            "customer.subscription.deleted" -> {
                subscriptionService.handleSubscriptionDeleted(sub.id)
            }

            "customer.subscription.created",
            "customer.subscription.updated" -> {
                subscriptionService.upsertFromStripe(
                    snapshot = snapshot,
                    cancelAtPeriodEnd = sub.cancelAtPeriodEnd || sub.cancelAt != null,
                )
            }
        }
    }
}