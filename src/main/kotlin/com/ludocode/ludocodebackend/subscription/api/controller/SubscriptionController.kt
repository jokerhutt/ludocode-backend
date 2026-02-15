package com.ludocode.ludocodebackend.subscription.api.controller

import com.ludocode.ludocodebackend.commons.configuration.AppProps
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.subscription.api.dto.request.CheckoutRequest
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(ApiPaths.SUBSCRIPTION.BASE)
class SubscriptionController(private val subscriptionPlanRepository: SubscriptionPlanRepository,
                             private val appProps: AppProps
) {

    @PostMapping(ApiPaths.SUBSCRIPTION.CHECKOUT)
    fun createCheckoutSession(@AuthenticationPrincipal(expression = "userId") userId: UUID, @RequestBody request: CheckoutRequest): Map<String, String> {

        val frontendUrl = appProps.frontendUrl

        val plan = subscriptionPlanRepository
            .findByPlanCodeAndIsActiveTrue(request.planCode)
            ?: throw ApiException(ErrorCode.PLAN_NOT_FOUND)

        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl(frontendUrl + "/billing/success")
            .setCancelUrl(frontendUrl + "/billing/cancel")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(plan.stripePriceId)
                    .setQuantity(1)
                    .build()
            )
            .putMetadata("userId", userId.toString())
            .putMetadata("planId", plan.id.toString())
            .build()

        val session = Session.create(params)

        return mapOf("url" to session.url!!)
    }



}