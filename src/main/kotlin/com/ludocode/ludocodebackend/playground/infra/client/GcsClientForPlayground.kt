package com.ludocode.ludocodebackend.playground.infra.client

import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsDeleteRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.request.GcsPutRequestList
import com.ludocode.ludocodebackend.gcs.app.dto.response.UploadedPaths
import com.ludocode.ludocodebackend.playground.app.port.out.GcsPortForPlayground
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class GcsClientForPlayground (
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val gcsServiceBaseUrl: String
) : GcsPortForPlayground {

    override fun getContentFromUrls( paths: List<String>): Map<String, String> {

        val url = "$gcsServiceBaseUrl${InternalPathConstants.IGCS}/get-content?${paths.joinToString("&") { "paths=$it" }}"

        val response = rest.exchange(
            url,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<Map<String, String>>() {}
        )


        return response.body ?: emptyMap()
    }

    override fun getContentFromPath(path: String): String {


        val url = "$gcsServiceBaseUrl${InternalPathConstants.IGCS}/get-content-single?path=$path"

        println("URL IS: " + url)

        val response = rest.exchange(
            url,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<String>() {}
        )

        println("RES BODY: ${response.body}")

        return response.body ?: ""
    }

    override fun uploadDataList(reqs: GcsPutRequestList): UploadedPaths {
        val url = "$gcsServiceBaseUrl${InternalPathConstants.IGCS}${InternalPathConstants.IGCS_UPLOAD_FILES}"
        val resp = rest.postForEntity(url, reqs, UploadedPaths::class.java)
        return resp.body ?: error("Could not upload files")
    }

    override fun deleteDataList(req: GcsDeleteRequestList): UploadedPaths {
        val url = "$gcsServiceBaseUrl${InternalPathConstants.IGCS}${InternalPathConstants.IGCS_DELETE_FILES}"
        val resp = rest.postForEntity(url, req, UploadedPaths::class.java)
        return resp.body ?: error("Could not delete files")
    }





}