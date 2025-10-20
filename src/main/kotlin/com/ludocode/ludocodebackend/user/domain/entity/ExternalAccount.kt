package com.ludocode.ludocodebackend.user.domain.entity

import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "external_account",
    uniqueConstraints = [UniqueConstraint(columnNames = ["provider", "provider_user_id"])]
)
class ExternalAccount(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: AuthProvider,

    @Column(name = "provider_user_id", nullable = false)
    val providerUserId: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    fun toDomain() = ExternalAccount(id, userId, provider, providerUserId, createdAt)

    companion object {
        fun fromDomain(n: NewExternalAccount) =
            ExternalAccount(
                userId = n.userId,
                provider = n.provider,
                providerUserId = n.providerUserId
            )
    }
}