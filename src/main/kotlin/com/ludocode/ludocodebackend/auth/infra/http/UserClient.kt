package com.ludocode.ludocodebackend.auth.infra.http

import com.ludocode.ludocodebackend.auth.app.port.out.UserPort
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IUSERS
import com.ludocode.ludocodebackend.commons.constants.InternalPathConstants.IUSERS_FIND_CREATE
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Component
class UserClient(
    private val rest: RestTemplate,
    @Value("\${user.service.base-url}") private val userServiceBaseUrl: String
) : UserPort {

    override fun findOrCreate(req: FindOrCreateUserRequest): UserResponse {

        val url = "$userServiceBaseUrl$IUSERS$IUSERS_FIND_CREATE"
        val resp = rest.postForEntity(url, req, UserResponse::class.java)
        return resp.body ?: error("User service returned empty body")
    }

    override fun getById(id: UUID): UserResponse {
        val url = "$userServiceBaseUrl$IUSERS/$id"
        val resp = rest.getForEntity(url, UserResponse::class.java)
        return resp.body ?: error("User service returned empty body")
    }
}