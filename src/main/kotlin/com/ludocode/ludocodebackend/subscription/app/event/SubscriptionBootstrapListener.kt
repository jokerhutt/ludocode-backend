package com.ludocode.ludocodebackend.subscription.app.event
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.user.domain.event.UserRegisteredEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class SubscriptionBootstrapListener(
    private val subscriptionService: SubscriptionService
) {

    @Async
    @EventListener
    fun handle(event: UserRegisteredEvent) {
        subscriptionService.ensureSubscriptionExists(event.userId)
    }
}