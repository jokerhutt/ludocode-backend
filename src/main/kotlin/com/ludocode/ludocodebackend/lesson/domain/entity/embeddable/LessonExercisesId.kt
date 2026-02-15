package com.ludocode.ludocodebackend.lesson.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
data class LessonExercisesId(
    @Column(name = "lesson_id")
    var lessonId: UUID,

    @Column(name = "order_index")
    val orderIndex: Int
) : Serializable