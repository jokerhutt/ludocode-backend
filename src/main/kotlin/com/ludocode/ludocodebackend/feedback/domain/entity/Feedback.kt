package com.ludocode.ludocodebackend.feedback.domain.entity

import com.ludocode.ludocodebackend.feedback.domain.enums.FeedbackType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "feedback")
class Feedback (

    @Id
    val id: UUID,

    @Column(name = "content")
    val content: String,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "feedback_type")
    val feedbackType: FeedbackType,

    @Column(name = "entity_id")
    val entityId: UUID? = null

)