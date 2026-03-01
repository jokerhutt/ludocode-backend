package com.ludocode.ludocodebackend.runner.api.dto.response

import com.ludocode.ludocodebackend.runner.api.dto.response.PistonRun
import kotlinx.serialization.Serializable

@Serializable
data class PistonResponse(val run: PistonRun?)