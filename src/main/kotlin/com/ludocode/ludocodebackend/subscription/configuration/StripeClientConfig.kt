package com.ludocode.ludocodebackend.subscription.configuration

import com.ludocode.ludocodebackend.commons.configuration.AppProps
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeBillingPort
import com.ludocode.ludocodebackend.subscription.app.port.out.StripeCheckoutPort
import com.ludocode.ludocodebackend.subscription.infra.http.NoOpStripeBillingClient
import com.ludocode.ludocodebackend.subscription.infra.http.NoOpStripeCheckoutClient
import com.ludocode.ludocodebackend.subscription.infra.http.StripeBillingClient
import com.ludocode.ludocodebackend.subscription.infra.http.StripeCheckoutClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StripeClientConfig {

    // ---- Checkout ----

    @Bean
    @ConditionalOnProperty(prefix = "stripe", name = ["enabled"], havingValue = "true")
    fun stripeCheckoutClient(appProps: AppProps): StripeCheckoutPort =
        StripeCheckoutClient(appProps)

    @Bean
    @ConditionalOnProperty(
        prefix = "stripe",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun noOpStripeCheckoutClient(): StripeCheckoutPort =
        NoOpStripeCheckoutClient()


    // ---- Billing ----

    @Bean
    @ConditionalOnProperty(prefix = "stripe", name = ["enabled"], havingValue = "true")
    fun stripeBillingClient(appProps: AppProps): StripeBillingPort =
        StripeBillingClient(appProps)

    @Bean
    @ConditionalOnProperty(
        prefix = "stripe",
        name = ["enabled"],
        havingValue = "false",
        matchIfMissing = true
    )
    fun noOpStripeBillingClient(): StripeBillingPort =
        NoOpStripeBillingClient()

}