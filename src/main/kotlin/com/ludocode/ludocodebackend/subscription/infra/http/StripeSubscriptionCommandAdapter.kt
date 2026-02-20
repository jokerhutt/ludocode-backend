package com.ludocode.ludocodebackend.subscription.infra.http

import com.ludocode.ludocodebackend.subscription.app.port.out.StripeSubscriptionCommandPort
import com.stripe.model.Customer
import com.stripe.model.Subscription
import com.stripe.param.CustomerCreateParams
import com.stripe.param.SubscriptionCreateParams
import org.springframework.stereotype.Component

@Component
class StripeSubscriptionCommandAdapter : StripeSubscriptionCommandPort {

    override fun cancelSubscription(subscriptionId: String) {
        val stripeSub = Subscription.retrieve(subscriptionId)
        stripeSub.cancel()
    }

    override fun createCustomer(email: String, name: String?): String {

        val builder = CustomerCreateParams.builder()
            .setEmail(email)

        if (!name.isNullOrBlank()) {
            builder.setName(name)
        }

        val customer = Customer.create(builder.build())
        return customer.id
    }

    override fun createSubscription(customerId: String, priceId: String): String {

        val params = SubscriptionCreateParams.builder()
            .setCustomer(customerId)
            .addItem(
                SubscriptionCreateParams.Item.builder()
                    .setPrice(priceId)
                    .build()
            )
            .build()

        val subscription = Subscription.create(params)

        return subscription.id
    }


}