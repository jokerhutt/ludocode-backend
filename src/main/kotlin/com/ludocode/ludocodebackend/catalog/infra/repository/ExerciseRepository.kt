package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
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
}