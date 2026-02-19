package com.ludocode.ludocodebackend.subscription.domain.entity

import com.ludocode.ludocodebackend.subscription.domain.enum.Plan
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "subscription_plan")
class SubscriptionPlan(

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

    @Column(name = "display_price", nullable = false)
    var displayPrice: BigDecimal,

    @Column(name = "description", nullable = false)
    var description: String,

    @Column(name = "currency")
    var currency: String,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime

)



