package com.ludocode.ludocodebackend.playground.app.dto.client

import kotlinx.serialization.Serializable

@Serializable
data class PistonRun(
    val stdout: String = "",
    val stderr: String = "",
    val code: Int = 0
)