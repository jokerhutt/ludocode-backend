package com.ludocode.ludocodebackend.playground.app.port.out

import com.ludocode.ludocodebackend.playground.api.dto.piston.PistonRequest
import com.ludocode.ludocodebackend.playground.api.dto.piston.PistonResponse

interface PistonOutboundPort {
    fun execute(request: PistonRequest): PistonResponse
    fun listRuntimes(): List<Map<String, Any>>
}