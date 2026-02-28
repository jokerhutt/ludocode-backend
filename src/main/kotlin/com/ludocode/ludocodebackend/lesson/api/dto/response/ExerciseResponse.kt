package com.ludocode.ludocodebackend.lesson.api.dto.response

import com.ludocode.ludocodebackend.lesson.domain.jsonb.Block
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseInteraction
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import java.util.*

data class ExerciseResponse(
    val id: UUID,
    val version: Int,
    val orderIndex: Int,
    val blocks: List<Block>,
    val interaction: ExerciseInteraction?
)