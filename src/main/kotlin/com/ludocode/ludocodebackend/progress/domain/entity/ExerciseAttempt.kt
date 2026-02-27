package com.ludocode.ludocodebackend.progress.domain.entity

import com.ludocode.ludocodebackend.exercise.ExerciseAnswer
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "exercise_attempt")
class ExerciseAttempt(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "exercise_id", nullable = false)
    val exerciseId: UUID,

    @Column(name = "exercise_version", nullable = false)
    val exerciseVersion: Int,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "answer", columnDefinition = "jsonb", nullable = false)
    val answer: ExerciseAnswer,

    @Column(name = "is_correct", nullable = false)
    val isCorrect: Boolean,

    @Column(name = "submitted_at", nullable = false)
    val submittedAt: OffsetDateTime = OffsetDateTime.now()
)