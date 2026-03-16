package com.ludocode.ludocodebackend.runner.api.dto.request

import kotlinx.serialization.Serializable


data class PistonInitMessage(
    val type: String = "init",
    val language: String,
    val version: String,
    val files: List<PistonFile>,

    val run_timeout: Int = 120000,
    val run_cpu_time: Int = 120000,
    val compile_timeout: Int = 10000,
    val compile_cpu_time: Int = 10000,
    val run_memory_limit: Long = 536870912
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