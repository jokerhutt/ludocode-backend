package com.ludocode.ludocodebackend.subscription.app.port.out

interface  StripeBillingPort {
    fun createBillingPortalSession(customerId: String): String
}