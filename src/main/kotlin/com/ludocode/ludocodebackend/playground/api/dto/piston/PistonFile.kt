package com.ludocode.ludocodebackend.playground.api.dto.piston

import kotlinx.serialization.Serializable

@Serializable
data class PistonFile(val name: String, val content: String)