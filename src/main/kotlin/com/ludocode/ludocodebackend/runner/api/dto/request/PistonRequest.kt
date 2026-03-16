package com.ludocode.ludocodebackend.runner.api.dto.request

import kotlinx.serialization.Serializable


data class PistonInitMessage(
    val type: String = "init",
    val language: String,
    val version: String,
    val files: List<PistonFile>
)

data class PistonFile(
    val name: String,
    val content: String
)

data class PistonDataMessage(
    val type: String = "data",
    val stream: String,
    val data: String
)

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