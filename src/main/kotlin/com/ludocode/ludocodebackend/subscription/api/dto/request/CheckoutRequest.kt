package com.ludocode.ludocodebackend.subscription.api.dto.request

import com.ludocode.ludocodebackend.subscription.domain.enums.Plan

data class CheckoutRequest(val planCode: Plan)
