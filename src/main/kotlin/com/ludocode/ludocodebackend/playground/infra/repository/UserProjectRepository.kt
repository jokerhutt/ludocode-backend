package com.ludocode.ludocodebackend.playground.infra.repository

import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserProjectRepository : JpaRepository<UserProject, UUID> {

    @Query(
        value = """
        SELECT p.id
        FROM user_project p
        JOIN ludo_user u ON u.id = p.user_id
        WHERE p.user_id = :userId
            AND u.is_deleted = FALSE
        ORDER BY p.updated_at DESC
    """,
        nativeQuery = true
    )
    fun findProjectIdsByUserId(@Param("userId") userId: UUID): List<UUID>

}