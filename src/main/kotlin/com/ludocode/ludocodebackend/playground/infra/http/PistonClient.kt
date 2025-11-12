package com.ludocode.ludocodebackend.playground.infra.http

import com.ludocode.ludocodebackend.commons.constants.ExternalPathContstants
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonRequest
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonResponse
import com.ludocode.ludocodebackend.playground.app.dto.client.PistonRun
import com.ludocode.ludocodebackend.playground.app.port.out.PistonOutboundPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class HttpPistonOutboundClient : PistonOutboundPort {

    private val rest = RestTemplate()

    override fun execute(request: PistonRequest): PistonResponse {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(request, headers)
        return rest.postForObject(ExternalPathContstants.PISTON_EXECUTE, entity, PistonResponse::class.java)
            ?: PistonResponse(PistonRun(stderr = "Empty response"))
    }

    override fun listRuntimes(): List<Map<String, Any>> {
        val result = rest.getForObject(ExternalPathContstants.PISTON_RUNTIMES, List::class.java)
        @Suppress("UNCHECKED_CAST")
        return result as? List<Map<String, Any>> ?: emptyList()
    }



}