package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.infra.projection.ExerciseFlatProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface ExerciseRepository : JpaRepository<Exercise, UUID>{

    @Query("""
        SELECT *
        FROM exercise
        WHERE lesson_id = :lessonId
    """, nativeQuery = true)
    fun findAllByLessonId(lessonId: UUID): List<Exercise>

    @Query(
        value = """
    SELECT 
      e.id AS exerciseId, e.title AS title,e.prompt AS prompt, e.exercise_type AS exerciseType, e.lesson_id AS lessonId,
      o.id AS optionId, o.content AS content, o.answer_order AS answerOrder
    FROM exercise e
    LEFT JOIN exercise_option o ON o.exercise_id = e.id
    WHERE e.lesson_id = :lessonId
    """,
        nativeQuery = true
    )
    fun getFlatExercisesWithOptions(lessonId: UUID): List<ExerciseFlatProjection>




}