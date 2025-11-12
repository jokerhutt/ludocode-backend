package com.ludocode.ludocodebackend.playground.infra.http

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IGCS_GET_CONTENT_FROM_PATHS
import com.ludocode.ludocodebackend.playground.app.port.out.GcsPortForPlayground
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class GcsClientForPlayground (
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val gcsServiceBaseUrl: String
) : GcsPortForPlayground {

    override fun getContentFromUrls(bucket: String, paths: List<String>): Map<String, String> {
        val url = "$gcsServiceBaseUrl/$IGCS_GET_CONTENT_FROM_PATHS"

        val response: ResponseEntity<Map<String, String>> =
            rest.exchange(
                "$url?bucket={bucket}&" + paths.joinToString("&") { "paths=$it" },
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<Map<String, String>>() {},
                bucket
            )

        return response.body ?: emptyMap()
    }





}