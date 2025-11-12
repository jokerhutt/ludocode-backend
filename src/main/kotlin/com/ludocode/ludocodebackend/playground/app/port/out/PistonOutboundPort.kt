package com.ludocode.ludocodebackend.playground.app.port.out

import com.ludocode.ludocodebackend.playground.app.dto.client.PistonRequest
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonResponse

interface PistonOutboundPort {
    fun execute(request: PistonRequest): PistonResponse
    fun listRuntimes(): List<Map<String, Any>>
}