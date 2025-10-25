package com.ludocode.ludocodebackend.auth.infra.http

import com.ludocode.ludocodebackend.auth.app.port.out.UserPortForAuth
import com.ludocode.ludocodebackend.auth.app.port.out.UserStatsPortForAuth
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ISTATSPROGRESS
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStatsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.util.UUID

@Component
class UserStatsClientForAuth (
    private val rest: RestTemplate,
    @Value("\${user-stats.service.base-url}") private val userStatsServiceBaseUrl: String
) : UserStatsPortForAuth {

    override fun findOrCreateStats(userId: UUID): UserStatsResponse {
       val url = "$userStatsServiceBaseUrl$ISTATSPROGRESS/$userId/upsert"
       val resp = rest.postForEntity(url, null, UserStatsResponse::class.java)
       return resp.body ?: error("Could not upsert user stats")
    }

}