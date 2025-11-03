package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.LessonExercises
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.catalog.infra.projection.ExerciseFlatProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LessonExercisesRepository : JpaRepository<LessonExercises, LessonExercisesId> {

    @Query(
        value = """
    SELECT 
      e.id                  AS exerciseId,
      e.version             AS version,
      e.title               AS title,
      e.prompt              AS prompt,
      e.exercise_type       AS exerciseType,
      le.lesson_id          AS lessonId,
      oc.id                 AS optionId,
      oc.content            AS content
    FROM lesson_exercises le
    JOIN exercise e
      ON e.id = le.exercise_id
     AND e.version = le.exercise_version
    LEFT JOIN exercise_option eo
      ON eo.exercise_id = e.id
     AND eo.exercise_version = e.version
    LEFT JOIN option_content oc
      ON oc.id = eo.option_id
    WHERE le.lesson_id = :lessonId
      AND e.is_deleted = false
    ORDER BY le.order_index, e.id, oc.id
    """,
        nativeQuery = true
    )
    fun getFlatByLesson(@Param("lessonId") lessonId: UUID): List<ExerciseFlatProjection>

    @Query(
        value = """
        SELECT exercise.id
        FROM exercise
        JOIN lesson_exercises ON lesson_exercises.exercise_id = exercise.id
        WHERE lesson_exercises.lesson_id = :lessonId
          AND exercise.is_deleted = false
        """,
        nativeQuery = true
    )
    fun findActiveExercisesByLessonId(@Param("lessonId") lessonId: UUID): List<UUID>

    @Modifying
    @Query(
        value = """
        DELETE FROM lesson_exercises
        WHERE lesson_id = :lessonId
        """,
        nativeQuery = true
    )
    fun deleteExercisesInLesson(@Param("lessonId") lessonId: UUID)

}