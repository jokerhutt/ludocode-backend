package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ExerciseOptionRepository: JpaRepository<ExerciseOption, UUID> {

    @Query(
        value = """
    SELECT * FROM exercise_option
    WHERE exercise_id = ANY(:exerciseIds)
    ORDER BY exercise_id, id;
  """, nativeQuery = true
    )
    fun findOptionsForExerciseIds(@Param("exerciseIds") ids: List<UUID>): List<ExerciseOption>

    @Modifying
    @Query(
        value = "delete from exercise_option where exercise_id = :exerciseId and exercise_version = :version",
        nativeQuery = true
    )
    fun deleteByExerciseIdAndVersion(
        @Param("exerciseId") exerciseId: UUID,
        @Param("version") version: Int
    )




}