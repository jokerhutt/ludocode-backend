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
    JsonSubTypes.Type(value = ClozeInteraction::class, name = "CLOZE"),
    JsonSubTypes.Type(value = ExecutableInteraction::class, name = "EXECUTABLE")
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
    val options: List<String>,
    val output: String? = null
) : ExerciseInteraction

data class InteractionBlank(
    val index: Int,
    val correctOptions: List<String>
)

data class InteractionFile(
    val language: String,
    val content: String
)

data class ExecutableFile(
    val name: String,
    val language: String,
    val content: String
)

data class ExecutableTest(
    val type: TestType,
    val expected: String
)

data class ExecutableInteraction(
    override val clientId: UUID = UUID.randomUUID(),
    val files: List<ExecutableFile>,
    val tests: List<ExecutableTest>,
    val showOutput: Boolean = true
) : ExerciseInteraction

enum class TestType {
    OUTPUT_EQUALS,
    OUTPUT_CONTAINS,
    FILE_CONTAINS
}