package com.ludocode.ludocodebackend.subscription.infra.repository

import com.ludocode.ludocodebackend.subscription.domain.entity.UserSubscription
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserSubscriptionRepository : JpaRepository<UserSubscription, UUID> {

    fun findByUserId(userId: UUID): UserSubscription?

}