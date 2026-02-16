package com.ludocode.ludocodebackend.subscription.domain.entity

import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "subscription_plan")
class SubscriptionPlan (

    @Id
    var id: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_code", nullable = false)
    var planCode: Plan,

    @Column(name = "display_name", nullable = false)
    var displayName: String,

    @Column(name = "stripe_price_id", nullable = false, unique = true)
    var stripePriceId: String,

    @Column(name = "billing_interval", nullable = false)
    var billingInterval: String,

    @Column(name = "ai_monthly_credit_limit", nullable = false)
    var aiMonthlyCreditlimit: Int,

    @Column(name = "max_projects", nullable = false)
    var maxProjects: Int,

    @Column(name = "priority_support", nullable = false)
    var prioritySupport: Boolean,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime

)



