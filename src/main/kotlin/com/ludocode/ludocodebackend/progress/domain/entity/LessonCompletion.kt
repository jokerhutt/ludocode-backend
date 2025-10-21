package com.ludocode.ludocodebackend.progress.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "lesson_completion")
class LessonCompletion(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(name = "lesson_id")
    var lessonId: UUID? = null,

    @Column(name = "score")
    var score: Int? = null,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null
)