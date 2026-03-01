package com.ludocode.ludocodebackend.runner.api.dto.request

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
)

@Serializable
data class PistonFile(val name: String, val content: String)