package com.ludocode.ludocodebackend.playground.app.dto.client

import kotlinx.serialization.Serializable


@Serializable
data class PistonResponse(val run: PistonRun?)
