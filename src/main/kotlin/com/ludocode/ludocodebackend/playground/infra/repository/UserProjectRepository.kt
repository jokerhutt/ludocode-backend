package com.ludocode.ludocodebackend.playground.infra.repository

import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserProjectRepository : JpaRepository<UserProject, UUID> {

    @Query(value = "SELECT name FROM user_project WHERE id = :projectId", nativeQuery = true)
    fun getProjectNameById (@Param("projectId") projectId: UUID): String

    @Query(value = "SELECT project_language FROM user_project WHERE id = :projectId", nativeQuery = true)
    fun getProjectLanaguageById(@Param("projectId") projectId: UUID): LanguageType

    @Query(value = "SELECT id FROM user_project WHERE user_id = :userId", nativeQuery = true)
    fun findAllByUserId (@Param("userId") userId: UUID): List<UUID>

}