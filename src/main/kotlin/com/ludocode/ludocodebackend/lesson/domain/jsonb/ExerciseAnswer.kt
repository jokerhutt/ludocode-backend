package com.ludocode.ludocodebackend.lesson.domain.jsonb

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SelectAnswer::class, name = "SELECT"),
    JsonSubTypes.Type(value = ClozeAnswer::class, name = "CLOZE"),
    JsonSubTypes.Type(value = ExecutableAnswer::class, name = "EXECUTABLE")
)
sealed interface ExerciseAnswer

data class SelectAnswer(
    val pickedValue: String
) : ExerciseAnswer

data class ClozeAnswer(
    val valuesByBlank: List<String>
) : ExerciseAnswer

data class ExecutableAnswer(
    val files: List<SubmittedFile>
) : ExerciseAnswer

data class SubmittedFile(
    val name: String,
    val content: String
)