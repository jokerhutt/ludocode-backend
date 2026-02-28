package com.ludocode.ludocodebackend.lesson.api.dto.snapshot

import com.ludocode.ludocodebackend.lesson.domain.jsonb.Block
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseInteraction
import java.util.UUID

data class ExerciseSnap (

    val exerciseId: UUID,

    val exerciseVersion: Int,

    val blocks: List<BlockSnap>,

    var interaction: InteractionSnap? = null
)

data class BlockSnap(
    val clientId: UUID = UUID.randomUUID(),
    val block: Block
)

data class InteractionSnap(
    val clientId: UUID = UUID.randomUUID(),
    val interaction: ExerciseInteraction
)
