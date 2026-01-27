package com.ludocode.ludocodebackend.playground.api.dto.piston

import kotlinx.serialization.Serializable

@Serializable
data class PistonRun(
    val stdout: String = "",
    val stderr: String = "",
    val code: Int = 0
)