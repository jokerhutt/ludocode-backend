package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.LessonExercisesId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "lesson_exercises")
class LessonExercises (

    @EmbeddedId
    val lessonExercisesId: LessonExercisesId,

    @Column(name = "exercise_id", nullable = false, unique = true)
    val exerciseId: UUID,

    @Column(name = "exercise_version", nullable = false, unique = true)
    val exerciseVersion: Int


)