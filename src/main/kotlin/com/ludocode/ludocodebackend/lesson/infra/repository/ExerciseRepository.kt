package com.ludocode.ludocodebackend.lesson.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ExerciseRepository : JpaRepository<Exercise, ExerciseId> {

    @Query(
        value = """
        SELECT *
        FROM exercise
        WHERE id = :id
          AND is_deleted = false
        ORDER BY version_number DESC
        LIMIT 1
        """,
        nativeQuery = true
    )
    fun findLatestActiveById(@Param("id") id: UUID): Exercise?

}