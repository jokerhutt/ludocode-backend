package com.ludocode.ludocodebackend.lesson.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.LessonExercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.infra.projection.ExerciseFlatProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface LessonExercisesRepository : JpaRepository<LessonExercise, LessonExercisesId> {

    @Query("""
    SELECT e
    FROM LessonExercise le
    JOIN Exercise e
      ON e.exerciseId.id = le.exerciseId
     AND e.exerciseId.versionNumber = le.exerciseVersion
    WHERE le.lessonExercisesId.lessonId = :lessonId
    ORDER BY le.lessonExercisesId.orderIndex ASC
""")
    fun findExercisesByLesson(@Param("lessonId") lessonId: UUID): List<Exercise>

    fun findByExerciseId(exerciseId: UUID): LessonExercise?

    fun deleteAllByLessonExercisesIdLessonId(lessonId: UUID)

}