package com.ludocode.ludocodebackend.subscription.domain.entity
import com.ludocode.ludocodebackend.subscription.domain.enum.SubscriptionLimit
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
@Table(name = "subscription_plan_limit")
class SubscriptionPlanLimit (

    @Id
    val id: UUID,

    @Column(name = "plan_id")
    val planId: UUID,

    @Column(name = "title")
    val title: String,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
        name = "limit_code",
        columnDefinition = "subscription_limit"
    )
    val limitCode: SubscriptionLimit,

    @Column(name = "limit_value")
    val limitValue: Int,

    )
