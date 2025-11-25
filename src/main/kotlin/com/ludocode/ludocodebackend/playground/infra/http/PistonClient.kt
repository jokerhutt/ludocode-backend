package com.ludocode.ludocodebackend.playground.infra.http

import com.ludocode.ludocodebackend.commons.constants.ExternalPathContstants.PISTON_EXECUTE
import com.ludocode.ludocodebackend.commons.constants.ExternalPathContstants.PISTON_RUNTIMES
import com.ludocode.ludocodebackend.playground.app.dto.piston.PistonRequest
import com.ludocode.ludocodebackend.playground.app.dto.piston.PistonResponse
import com.ludocode.ludocodebackend.playground.app.dto.piston.PistonRun
import com.ludocode.ludocodebackend.playground.app.port.out.PistonOutboundPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

class PistonClient (
    private val pistonBase: String
) : PistonOutboundPort {

    private val rest = RestTemplate()

    val executePath = "$pistonBase$PISTON_EXECUTE"
    val runtimesPath = "$pistonBase$PISTON_RUNTIMES"

    override fun execute(request: PistonRequest): PistonResponse {

        println("EXECUTE PATH: $executePath")
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(request, headers)

        return rest.postForObject(executePath, entity, PistonResponse::class.java)
            ?: PistonResponse(PistonRun(stderr = "Empty response"))
    }

    override fun listRuntimes(): List<Map<String, Any>> {
        println("RUNTIME PATH: $runtimesPath")
        val result = rest.getForObject(runtimesPath, List::class.java)
        @Suppress("UNCHECKED_CAST")
        return result as? List<Map<String, Any>> ?: emptyList()
    }



}