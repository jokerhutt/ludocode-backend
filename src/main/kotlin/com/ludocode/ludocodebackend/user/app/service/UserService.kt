package com.ludocode.ludocodebackend.user.app.service

import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.mapper.UserMapper
import com.ludocode.ludocodebackend.user.app.port.`in`.UserUseCase
import com.ludocode.ludocodebackend.user.domain.entity.ExternalAccount
import com.ludocode.ludocodebackend.user.domain.entity.User
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,              // ✅ JPA direct
    private val externalAccountRepository: ExternalAccountRepository, // ✅ JPA direct
    private val userMapper: UserMapper
) : UserUseCase {

    @Transactional
    override fun findOrCreate(req: FindOrCreateUserRequest): UserResponse {

        val existingUser : ExternalAccount? = externalAccountRepository.findByProviderAndProviderUserId(req.provider, req.providerUserId)

        if (existingUser != null) return getById(existingUser.userId)

        val newUser = userRepository.save(
            User(
                email = req.email ?: "",
                firstName = req.firstName ?: "",
                lastName = req.lastName ?: "",
                pfpSrc = req.avatarUrl,
                createdAt = OffsetDateTime.now(),
                currentCourse = null
            )
        )

        externalAccountRepository.save(
            ExternalAccount(
                userId = newUser.id!!,
                provider = req.provider,
                providerUserId = req.providerUserId
            )
        )

        return userMapper.toUserResponse(newUser)


    }

    override fun getById(id: UUID): UserResponse {
       return userMapper.toUserResponse(userRepository.findById(id).orElseThrow())
    }
}