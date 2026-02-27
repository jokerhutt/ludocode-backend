package com.ludocode.ludocodebackend.exercise

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SelectInteraction::class, name = "SELECT"),
    JsonSubTypes.Type(value = ClozeInteraction::class, name = "CLOZE")
)
sealed interface ExerciseInteraction

data class SelectInteraction(
    val items: List<String>,
    val correctValue: String
) : ExerciseInteraction

data class ClozeInteraction(
    val file: InteractionFile,
    val blanks: List<InteractionBlank>,
    val options: List<String>
) : ExerciseInteraction

data class InteractionBlank(
    val index: Int,
    val correctOptions: List<String>
)

data class InteractionFile(
    val language: String,
    val content: String
)