package com.ludocode.ludocodebackend.subscription.infra.repository

import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlan
import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubscriptionPlanRepository : JpaRepository<SubscriptionPlan, UUID> {

    fun findByPlanCodeAndIsActiveTrue(planCode: Plan): SubscriptionPlan?


}