package com.ludocode.ludocodebackend.lesson.domain.jsonb

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = MCQAnswer::class, name = "MCQ"),
    JsonSubTypes.Type(value = ClozeAnswer::class, name = "CLOZE")
)
sealed interface ExerciseAnswer

data class MCQAnswer(
    val pickedValue: String
) : ExerciseAnswer

data class ClozeAnswer(
    val valuesByBlank: List<String>
) : ExerciseAnswer