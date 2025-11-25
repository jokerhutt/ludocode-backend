package com.ludocode.ludocodebackend.playground.app.dto.piston

import kotlinx.serialization.Serializable

@Serializable
data class PistonFile(val name: String, val content: String)