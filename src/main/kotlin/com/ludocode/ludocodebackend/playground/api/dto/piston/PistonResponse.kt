package com.ludocode.ludocodebackend.playground.api.dto.piston

import kotlinx.serialization.Serializable


@Serializable
data class PistonResponse(val run: PistonRun?)
