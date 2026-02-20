package com.ludocode.ludocodebackend.subscription.app.port.out

interface StripeWebhookPort {
    fun handle(payload: String, signature: String)
}