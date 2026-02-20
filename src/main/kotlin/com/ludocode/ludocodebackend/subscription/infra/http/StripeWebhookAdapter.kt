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

        when (event.type) {

            "invoice.paid" -> {
                val invoice = event.dataObjectDeserializer
                    .getObject()
                    .orElse(null) as? Invoice
                    ?: return

                val subscriptionId = invoice.parent
                    ?.subscriptionDetails
                    ?.subscription
                    ?: return

                subscriptionService.handleInvoicePaid(
                    subscriptionId
                )
            }

            "customer.subscription.deleted" -> {
                val sub = event.dataObjectDeserializer
                    .getObject()
                    .orElse(null) as? Subscription
                    ?: return

                subscriptionService.handleSubscriptionDeleted(sub.id)
            }

            "customer.subscription.updated" -> {

                val sub = event.dataObjectDeserializer
                    .getObject()
                    .orElse(null) as? Subscription
                    ?: return

                val snapshot = stripeSubscriptionPort.retrieveSnapshot(sub.id)

                subscriptionService.handleSubscriptionUpdated(
                    snapshot = snapshot,
                    cancelAtPeriodEnd = sub.cancelAtPeriodEnd || sub.cancelAt != null,
                    isActive = sub.status == "active"
                )
            }

            "checkout.session.completed" -> {
                val session = event.dataObjectDeserializer
                    .getObject()
                    .orElse(null) as? Session
                    ?: return

                val metadata = session.metadata ?: return

                val userId = metadata["userId"]
                    ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                    ?: return

                val subscriptionId = session.subscription as? String ?: return

                val subscriptionSnapshot = stripeSubscriptionPort.retrieveSnapshot(subscriptionId)

                subscriptionService.handleCheckoutComplete(
                    userId = userId,
                    subscriptionSnapshot = subscriptionSnapshot
                )
            }
        }
    }
}