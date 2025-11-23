package com.ludocode.ludocodebackend.ai.infra.client

import com.ludocode.ludocodebackend.ai.app.port.out.ProjectsPortForAI
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class ProjectsClientForAI(
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val baseUrl: String
) : ProjectsPortForAI {

    override fun getFileContentById(fileId: UUID): String {
        val url = "$baseUrl${InternalPathConstants.IPROJECTS}/$fileId/content"

        val response = rest.exchange(
            url,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<String>() {}
        )

        return response.body ?: ""
    }
}