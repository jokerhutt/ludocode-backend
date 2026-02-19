package com.ludocode.ludocodebackend.progress.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "lesson_completion")
class LessonCompletion(

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "submission_id")
    var submissionId: UUID,

    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(name = "lesson_id")
    var lessonId: UUID? = null,

    @Column(name = "score")
    var score: Int? = null,

    @Column(name = "accuracy")
    var accuracy: BigDecimal,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    @Column(name = "is_deleted")
    var isDeleted: Boolean? = false,

    @Column(name = "course_id")
    var courseId: UUID? = null

)