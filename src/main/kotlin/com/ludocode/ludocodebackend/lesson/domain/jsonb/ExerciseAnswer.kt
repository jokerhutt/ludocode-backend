package com.ludocode.ludocodebackend.lesson.domain.jsonb

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot

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
    val submission: ProjectSnapshot
) : ExerciseAnswer