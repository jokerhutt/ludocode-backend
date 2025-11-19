package com.ludocode.ludocodebackend.auth.infra.http

import com.ludocode.ludocodebackend.auth.app.port.out.UserCoinsPortForAuth
import com.ludocode.ludocodebackend.auth.app.port.out.UserStreakPortForAuth
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ICOINSPROGRESS
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.ISTREAKPROGRESS
import com.ludocode.ludocodebackend.progress.api.dto.response.UserCoinsResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.UserStreakResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class UserStreakClientForAuth (
    private val rest: RestTemplate,
    @Value("\${user-stats.service.base-url}") private val userStreakServiceBaseUrl: String
) : UserStreakPortForAuth {

    override fun initializeStreak(userId: UUID): UserStreakResponse {
        val url = "$userStreakServiceBaseUrl$ISTREAKPROGRESS/$userId/initialize"
        val resp = rest.postForEntity(url, null, UserStreakResponse::class.java)
        return resp.body ?: error("Could not initialize user streak")
    }

}