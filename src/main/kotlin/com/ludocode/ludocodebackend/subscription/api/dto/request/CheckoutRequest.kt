package com.ludocode.ludocodebackend.subscription.api.dto.request

import com.ludocode.ludocodebackend.subscription.domain.enum.Plan

data class CheckoutRequest(val planCode: Plan)
