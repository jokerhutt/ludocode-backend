package com.ludocode.ludocodebackend.lesson.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ExerciseRepository : JpaRepository<Exercise, ExerciseId> {

    fun findTopByExerciseId_IdAndIsDeletedFalseOrderByExerciseId_VersionNumberDesc(
        id: UUID
    ): Exercise?
    fun existsByExerciseId_IdAndIsDeletedFalse(id: UUID): Boolean
}