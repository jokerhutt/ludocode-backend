package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.infra.projection.ExerciseFlatProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ExerciseRepository : JpaRepository<Exercise, ExerciseId>{

    @Query(
        value = """
  SELECT 
    e.id AS exerciseId,
    e.version AS version,
    e.title AS title,
    e.prompt AS prompt,
    e.exercise_type AS exerciseType,
    e.lesson_id AS lessonId,
    o.id AS optionId,
    o.content AS content,
    o.answer_order AS answerOrder
  FROM exercise e
  LEFT JOIN exercise_option o
    ON o.exercise_id = e.id
   AND o.exercise_version = e.version
  WHERE e.lesson_id = :lessonId
    AND e.version = (
      SELECT MAX(e2.version)
      FROM exercise e2
      WHERE e2.id = e.id
    )
    AND e.is_deleted = false
  ORDER BY e.id, o.answer_order
  """,
        nativeQuery = true
    )
    fun getFlatExercisesWithOptions(lessonId: UUID): List<ExerciseFlatProjection>


    @Query(
        value = """
  SELECT COALESCE(MAX(e.version), 0) + 1
  FROM exercise e
  WHERE e.id = :exerciseId
  """,
        nativeQuery = true
    )
    fun bumpVersion(exerciseId: UUID): Int







}