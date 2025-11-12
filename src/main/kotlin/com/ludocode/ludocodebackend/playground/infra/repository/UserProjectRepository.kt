package com.ludocode.ludocodebackend.playground.infra.repository

import com.ludocode.ludocodebackend.playground.domain.entity.UserProject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserProjectRepository : JpaRepository<UserProject, UUID> {

    @Query(value = "SELECT name FROM user_project WHERE id = :projectId", nativeQuery = true)
    fun getProjectNameById (@Param("projectId") projectId: UUID): String

}