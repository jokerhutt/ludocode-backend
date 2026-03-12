package com.ludocode.ludocodebackend.analytics.domain.entity

import com.ludocode.ludocodebackend.analytics.domain.enums.AnalyticsEventKey
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "analytics_event")
class AnalyticsEvent (

    @Id
    var id: UUID = UUID.randomUUID(),

    @Enumerated(EnumType.STRING)
    @Column(name = "event")
    val event: AnalyticsEventKey,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "properties", columnDefinition = "jsonb")
    val properties: Map<String, Any> = emptyMap(),

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()

)