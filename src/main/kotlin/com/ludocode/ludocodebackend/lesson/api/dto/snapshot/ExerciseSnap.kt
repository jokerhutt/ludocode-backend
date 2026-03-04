package com.ludocode.ludocodebackend.lesson.api.dto.snapshot

import com.ludocode.ludocodebackend.lesson.domain.jsonb.Block
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseInteraction
import java.util.UUID

data class ExerciseSnap (

    val exerciseId: UUID? = null,

    val blocks: List<Block>,

    var interaction: ExerciseInteraction? = null
)