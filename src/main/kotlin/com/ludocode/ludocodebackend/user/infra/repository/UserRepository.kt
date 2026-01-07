package com.ludocode.ludocodebackend.user.infra.repository

import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {

    fun findAllByIdIn(ids: List<UUID>): List<User>

    @Query(value = """
        SELECT u.time_zone
        FROM ludo_user u
        where u.id = :userId
        AND u.is_deleted = FALSE
        """, nativeQuery = true)
    fun findUserTimeZone(@Param("userId") userId: UUID): String?

    fun existsByIdAndIsDeletedFalse(id: UUID): Boolean

    fun findByEmail(email: String): User?

}