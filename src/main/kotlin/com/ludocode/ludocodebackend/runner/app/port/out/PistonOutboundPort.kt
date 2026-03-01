package com.ludocode.ludocodebackend.runner.app.port.out

import com.ludocode.ludocodebackend.runner.api.dto.request.PistonRequest
import com.ludocode.ludocodebackend.runner.api.dto.response.PistonResponse

interface PistonOutboundPort {
    fun execute(request: PistonRequest): PistonResponse
    fun listRuntimes(): List<Map<String, Any>>
}