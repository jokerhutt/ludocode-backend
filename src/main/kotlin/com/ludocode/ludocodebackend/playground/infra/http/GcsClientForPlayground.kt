package com.ludocode.ludocodebackend.playground.infra.http

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IGCS_DELETE_FILES
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IGCS_GET_CONTENT_FROM_PATHS
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IGCS_UPLOAD_FILES
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequest
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.UploadedPaths
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

    override fun uploadDataList(reqs: GcsPutRequestList): UploadedPaths {
        val url = "$gcsServiceBaseUrl/$IGCS_UPLOAD_FILES"
        val resp = rest.postForEntity(url, reqs, UploadedPaths::class.java)
        return resp.body ?: error("Could not upload files")
    }

    override fun deleteDataList(req: GcsDeleteRequestList): UploadedPaths {
        val url = "$gcsServiceBaseUrl/$IGCS_DELETE_FILES"
        val resp = rest.postForEntity(url, req, UploadedPaths::class.java)
        return resp.body ?: error("Could not delete files")
    }





}