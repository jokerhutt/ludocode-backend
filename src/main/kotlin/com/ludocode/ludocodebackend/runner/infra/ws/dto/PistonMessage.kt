package com.ludocode.ludocodebackend.runner.infra.ws.dto

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
