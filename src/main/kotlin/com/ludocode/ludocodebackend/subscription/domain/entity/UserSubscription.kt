package com.ludocode.ludocodebackend.subscription.domain.entity

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "user_subscription")
class UserSubscription(

    @Id
    var id: UUID,

    @Column(name = "user_id", nullable = false, unique = true)
    var userId: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: SubscriptionPlan,

    @Column(name = "stripe_subscription_id", nullable = false, unique = true)
    var stripeSubscriptionId: String,

    @Column(name = "status", nullable = false)
    var status: String,

    @Column(name = "current_period_start", nullable = false)
    var currentPeriodStart: OffsetDateTime,

    @Column(name = "current_period_end", nullable = false)
    var currentPeriodEnd: OffsetDateTime,

    @Column(name = "cancel_at_period_end", nullable = false)
    var cancelAtPeriodEnd: Boolean = false,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime
)