package com.ludocode.ludocodebackend.auth.infra.client

import com.ludocode.ludocodebackend.auth.app.port.out.UserCoinsPortForAuth
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class UserCoinsClientForAuth (
    private val rest: RestTemplate,
    @Value("\${user-stats.service.base-url}") private val userCoinsServiceBaseUrl: String
) : UserCoinsPortForAuth {

    override fun findOrCreateCoins(userId: UUID): UserCoinsResponse {
       val url = "$userCoinsServiceBaseUrl${InternalPathConstants.ICOINSPROGRESS}/$userId/upsert"
       val resp = rest.postForEntity(url, null, UserCoinsResponse::class.java)
       return resp.body ?: error("Could not upsert user coins")
    }

}