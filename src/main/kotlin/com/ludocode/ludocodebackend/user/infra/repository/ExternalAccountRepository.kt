package com.ludocode.ludocodebackend.user.infra.repository

import com.ludocode.ludocodebackend.user.domain.entity.ExternalAccount
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ExternalAccountRepository : JpaRepository<ExternalAccount, UUID> {

    fun findByProviderAndProviderUserId(
        provider: AuthProvider,
        providerUserId: String
    ): ExternalAccount?

    fun findByUserId(userId: UUID): ExternalAccount?

}