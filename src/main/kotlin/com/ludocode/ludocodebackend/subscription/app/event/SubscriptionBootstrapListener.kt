package com.ludocode.ludocodebackend.subscription.app.event
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.user.domain.event.UserRegisteredEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class SubscriptionBootstrapListener(
    private val subscriptionService: SubscriptionService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: UserRegisteredEvent) {
        subscriptionService.ensureSubscriptionExists(event.userId)
    }
}