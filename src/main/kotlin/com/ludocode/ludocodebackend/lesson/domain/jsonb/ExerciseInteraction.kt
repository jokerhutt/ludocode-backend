package com.ludocode.ludocodebackend.lesson.domain.jsonb

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata

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

data class ExecutableInteraction(
    override val clientId: UUID = UUID.randomUUID(),
    val tests: List<ExecutableTest>,
    val showOutput: Boolean = true
) : ExerciseInteraction

data class InteractionBlank(
    val index: Int,
    val correctOptions: List<String>
)

data class InteractionFile(
    val language: String,
    val content: String
)

data class ExecutableTest(
    val type: TestType,
    val expected: String,
    val feedback: String? = "Not quite!"
)

enum class TestType {
    OUTPUT_CONTAINS,
    FILE_CONTAINS,
    FILE_PATTERN_MATCHES,
    OUTPUT_PATTERN_MATCHES
}