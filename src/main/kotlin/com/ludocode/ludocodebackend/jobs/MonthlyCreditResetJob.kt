package com.ludocode.ludocodebackend.jobs
import com.ludocode.ludocodebackend.ai.app.port.out.AiCreditPortForSubscription
import com.ludocode.ludocodebackend.subscription.app.service.SubscriptionService
import com.ludocode.ludocodebackend.subscription.configuration.PlanDefinitions
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class MonthlyCreditResetJob(
    private val userRepository: UserRepository,
    private val subscriptionService: SubscriptionService,
    private val aiCreditPortForSubscription: AiCreditPortForSubscription,
) {

    @Transactional
    fun execute() {

        val users = userRepository.findAll()

        val freePlanLimit = PlanDefinitions.configFor(Plan.FREE).limits.monthlyAiCredits

        users.forEach { user ->
            if (subscriptionService.isFreeUser(user.id)) {
                aiCreditPortForSubscription.resetCredits(user.id, freePlanLimit)
            }
        }
    }
}