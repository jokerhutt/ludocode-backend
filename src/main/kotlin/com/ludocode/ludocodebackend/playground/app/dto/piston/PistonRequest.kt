package com.ludocode.ludocodebackend.playground.app.dto.piston

import kotlinx.serialization.Serializable

@Serializable
data class PistonRequest(
    val language: String,
    val version: String = "*",
    val files: List<PistonFile>,
    val stdin: String = "",
    val args: List<String> = emptyList(),
    val compile_timeout: Int = 3000,
    val run_timeout: Int = 3000,
    val compile_memory_limit: Int = 256,
    val run_memory_limit: Int = 256
)
