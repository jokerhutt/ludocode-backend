package com.ludocode.ludocodebackend.subscription.infra.repository

import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlanLimit
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubscriptionPlanLimitRepository : JpaRepository<SubscriptionPlanLimit, UUID> {

    fun findByPlanId(planId: UUID): List<SubscriptionPlanLimit>
    fun findAllByPlanIdIn(planIds: List<UUID>): List<SubscriptionPlanLimit>


}