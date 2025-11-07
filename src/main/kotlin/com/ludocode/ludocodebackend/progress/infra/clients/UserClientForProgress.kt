package com.ludocode.ludocodebackend.progress.infra.clients

import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICATALOG
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IUSERS
import com.ludocode.ludocodebackend.progress.app.port.out.UserPortForProgress
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class UserClientForProgress (
    private val rest: RestTemplate,
    @Value("\${catalog.service.base-url}") private val userServiceBaseUrl: String
) : UserPortForProgress {

    override fun getUserTimezone(userId: UUID): String? {
        val url = "$userServiceBaseUrl$IUSERS/$userId/timezone"
        val resp = rest.getForEntity(url, String::class.java)
        return resp.body ?: error("Could not find user timezone")
    }

}