package com.ludocode.ludocodebackend.subscription.infra.repository

import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UserSubscriptionRepository : JpaRepository<UserSubscription, UUID> {

    fun findByUserId(userId: UUID): UserSubscription?

    @Query(
        """
    select us from UserSubscription us
    join fetch us.plan
    where us.userId = :userId
"""
    )
    fun findByUserIdWithPlan(userId: UUID): UserSubscription?

    fun findByStripeSubscriptionId(stripeSubscriptionId: String): UserSubscription?

}