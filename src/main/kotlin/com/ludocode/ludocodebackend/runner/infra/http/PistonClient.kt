package com.ludocode.ludocodebackend.runner.infra.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ludocode.ludocodebackend.commons.constants.ExternalPathConstants
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.runner.api.dto.request.PistonRequest
import com.ludocode.ludocodebackend.runner.api.dto.response.PistonResponse
import com.ludocode.ludocodebackend.runner.api.dto.response.PistonRun
import com.ludocode.ludocodebackend.runner.app.port.out.PistonOutboundPort
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

@ConditionalOnProperty(prefix = "piston", name = ["enabled"], havingValue = "true")
class PistonClient(
    private val pistonBase: String
) : PistonOutboundPort {

    private val logger = LoggerFactory.getLogger(PistonClient::class.java)

    private val rest = RestTemplate()
    private val mapper = jacksonObjectMapper()

    val executePath = "$pistonBase${ExternalPathConstants.PISTON_EXECUTE}"
    val runtimesPath = "$pistonBase${ExternalPathConstants.PISTON_RUNTIMES}"

    override fun execute(request: PistonRequest): PistonResponse {

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val entity = HttpEntity(request, headers)

        try {
            val resp = rest.postForObject(executePath, entity, PistonResponse::class.java)
                ?: run {
                    logger.warn(LogEvents.PISTON_EMPTY_RESPONSE)
                    return PistonResponse(PistonRun(stderr = "Empty response"))
                }

            return resp
        } catch (e: Exception) {
            logger.error(LogEvents.PISTON_EXECUTE_FAILED, e)
            throw e
        }
    }

    override fun listRuntimes(): List<Map<String, Any>> {
        return try {
            val result = rest.getForObject(runtimesPath, List::class.java)
            @Suppress("UNCHECKED_CAST")
            result as? List<Map<String, Any>> ?: emptyList()
        } catch (e: Exception) {
            logger.error(LogEvents.PISTON_RUNTIMES_FAILED, e)
            emptyList()
        }
    }


}