package com.ludocode.ludocodebackend.subscription.domain.entity

import com.ludocode.ludocodebackend.subscription.domain.enum.SubscriptionFeature
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "subscription_plan_feature")
class SubscriptionPlanFeature (

    @Id
    var id: UUID,

    @Column(name = "plan_id")
    var planId: UUID,

    @Column(name = "title")
    var title: String,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        name = "feature_code",
        columnDefinition = "subscription_feature"
    )
    var featureCode: SubscriptionFeature,

    @Column(name = "enabled")
    var enabled: Boolean,

    )




