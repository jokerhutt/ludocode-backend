package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.LessonExercises
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.LessonExercisesId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LessonExercisesRepository : JpaRepository<LessonExercises, LessonExercisesId> {

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