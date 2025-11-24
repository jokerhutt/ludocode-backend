package com.ludocode.ludocodebackend.catalog.app.port.`in`

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import java.util.UUID

interface SnapshotUseCase {
    fun findExerciseSnapshotById(exerciseId: UUID) : ExerciseSnap
}