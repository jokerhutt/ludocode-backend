package com.ludocode.ludocodebackend.lesson.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.lesson.infra.projection.ExerciseFlatProjection
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LessonExercisesRepository : JpaRepository<LessonExercise, LessonExercisesId> {

    @Query(value = """
    SELECT 
      e.id            AS exerciseId,
      e.version_number AS version,
      e.title         AS title,
      e.prompt        AS prompt,
      e.exercise_type AS exerciseType,
      e.exercise_media AS exerciseMedia,
      e.subtitle      AS subtitle,
      le.lesson_id    AS lessonId,
      le.order_index  AS orderIndex,
      eo.id           AS optionId,
      oc.content      AS content,
      eo.answer_order AS answerOrder
    FROM lesson_exercises le
    JOIN exercise e
      ON e.id = le.exercise_id
     AND e.version_number = le.exercise_version
    LEFT JOIN exercise_option eo
      ON eo.exercise_id = e.id
     AND eo.exercise_version = e.version_number
    LEFT JOIN option_content oc
      ON oc.id = eo.option_id
    WHERE le.lesson_id = :lessonId
      AND e.is_deleted = false
    ORDER BY le.order_index ASC, e.id ASC
""", nativeQuery = true)
    fun getFlatExercisesWithOptions(
        @Param("lessonId") lessonId: UUID
    ): List<ExerciseFlatProjection>

    @Query(
        value = """
    SELECT 
      e.id              AS exerciseId,
      e.version_number  AS version,
      e.title           AS title,
      e.prompt          AS prompt,
      e.exercise_type   AS exerciseType,
      e.exercise_media  AS exerciseMedia,
      e.subtitle        AS subtitle,
      eo.id             AS optionId,
      oc.content        AS content,
      eo.answer_order   AS answerOrder
    FROM exercise e
    LEFT JOIN exercise_option eo
      ON eo.exercise_id = e.id
     AND eo.exercise_version = e.version_number
    LEFT JOIN option_content oc
      ON oc.id = eo.option_id
    WHERE e.id = :exerciseId
      AND e.version_number = (
            SELECT MAX(version_number)
            FROM exercise
            WHERE id = :exerciseId
      )
      AND e.is_deleted = FALSE
    ORDER BY eo.answer_order ASC, eo.id ASC
    """,
        nativeQuery = true
    )
    fun getSingleExerciseNewestFlat(
        @Param("exerciseId") exerciseId: UUID
    ): List<ExerciseFlatProjection>

    fun deleteAllByLessonExercisesIdLessonId(lessonId: UUID)

}