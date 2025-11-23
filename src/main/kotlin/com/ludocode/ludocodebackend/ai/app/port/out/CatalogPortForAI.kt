package com.ludocode.ludocodebackend.ai.app.port.out

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import java.util.UUID

interface CatalogPortForAI {
    fun findExerciseSnapshotById(exerciseId: UUID) : ExerciseSnap
}