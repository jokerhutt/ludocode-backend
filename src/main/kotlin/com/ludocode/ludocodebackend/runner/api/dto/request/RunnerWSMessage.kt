package com.ludocode.ludocodebackend.runner.api.dto.request

sealed interface RunnerWSMessage {
    val type: String
}

data class RunnerRunMessage(
    override val type: String,
    val files: List<RunnerFile>
) : RunnerWSMessage

data class RunnerStdinMessage(
    override val type: String,
    val text: String
) : RunnerWSMessage

data class RunnerFile(
    val codeLanguage: String,
    val name: String,
    val content: String
)
