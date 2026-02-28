package com.ludocode.ludocodebackend.lesson.domain.jsonb

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

import java.util.UUID

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SelectInteraction::class, name = "SELECT"),
    JsonSubTypes.Type(value = ClozeInteraction::class, name = "CLOZE")
)
sealed interface ExerciseInteraction {
    val clientId: UUID
}

data class SelectInteraction(
    override val clientId: UUID = UUID.randomUUID(),
    val items: List<String>,
    val correctValue: String
) : ExerciseInteraction

data class ClozeInteraction(
    override val clientId: UUID = UUID.randomUUID(),
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