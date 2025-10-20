package com.ludocode.ludocodebackend.user.app.service

import com.ludocode.ludocodebackend.user.domain.entity.ExternalAccount
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExternalAccountRepository : JpaRepository<ExternalAccount, UUID> {

    fun findByProviderAndProviderUserId(
        provider: AuthProvider,
        providerUserId: String
    ): ExternalAccount?
}