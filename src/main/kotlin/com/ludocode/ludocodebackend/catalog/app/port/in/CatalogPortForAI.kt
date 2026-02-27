package com.ludocode.ludocodebackend.catalog.app.port.`in`

import com.ludocode.ludocodebackend.exercise.LExercise
import java.util.*

interface CatalogPortForAI {
    fun findExerciseSnapshotById(exerciseId: UUID):  LExercise
}