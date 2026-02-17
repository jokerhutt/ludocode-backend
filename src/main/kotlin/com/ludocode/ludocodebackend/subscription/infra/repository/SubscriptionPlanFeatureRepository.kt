package com.ludocode.ludocodebackend.subscription.infra.repository

import com.ludocode.ludocodebackend.subscription.domain.entity.SubscriptionPlanFeature
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubscriptionPlanFeatureRepository : JpaRepository<SubscriptionPlanFeature, UUID> {

    fun findAllByPlanIdIn(planIds: List<UUID>): List<SubscriptionPlanFeature>

}