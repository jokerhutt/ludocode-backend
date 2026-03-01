package com.ludocode.ludocodebackend.runner.api.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PistonRun(
    val stdout: String = "",
    val stderr: String = "",
    val code: Int = 0
)