package com.ludocode.ludocodebackend.playground.infra.repository

import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserProjectRepository : JpaRepository<UserProject, UUID> {

    @Query(value = "SELECT id FROM user_project WHERE user_id = :userId ORDER BY updated_at DESC", nativeQuery = true)
    fun findProjectIdsByUserId (@Param("userId") userId: UUID): List<UUID>

}