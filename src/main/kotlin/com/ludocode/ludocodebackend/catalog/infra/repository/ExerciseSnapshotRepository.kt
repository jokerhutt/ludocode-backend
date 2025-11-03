package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ExerciseSnapshotRepository : JpaRepository<Exercise, ExerciseId> {

    @Modifying
    @Query(value = """
        UPDATE exercise
        SET is_deleted = true
        WHERE id IN (:ids)
        """, nativeQuery = true)
    fun softDeleteExercisesByIds (@Param("ids") ids: List<UUID>): Int

    @Query(
        value = """
        SELECT *
        FROM exercise
        WHERE id = :id
          AND is_deleted = false
        ORDER BY version DESC
        LIMIT 1
        """,
        nativeQuery = true
    )
    fun findLatestActiveById(@Param("id") id: UUID): Exercise?


}