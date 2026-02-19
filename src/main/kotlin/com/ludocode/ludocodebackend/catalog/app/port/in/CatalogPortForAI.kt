package com.ludocode.ludocodebackend.catalog.app.port.`in`

import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import java.util.*

interface CatalogPortForAI {
    fun findExerciseSnapshotById(exerciseId: UUID): ExerciseSnap
}