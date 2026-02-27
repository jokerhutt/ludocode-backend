package com.ludocode.ludocodebackend.lesson.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ExerciseRepository : JpaRepository<Exercise, ExerciseId> {

    fun findTopByExerciseId_IdOrderByExerciseId_VersionDesc(
        id: UUID
    ): Exercise?

}