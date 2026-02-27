package com.ludocode.ludocodebackend.exercise

import java.util.UUID

data class LExercise (

    val exerciseId: UUID,

    val exerciseVersion: Int,

    val blocks: List<Block>,

    val interaction: ExerciseInteraction? = null
)