package com.ludocode.ludocodebackend.playground.infra.repository

import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ProjectFileRepository : JpaRepository<ProjectFile, UUID> {
    @Query("SELECT * FROM project_file WHERE project_id = :projectId", nativeQuery = true)
    fun findAllProjectFilesByProjectId(@Param("projectId") projectId: UUID): List<ProjectFile>

}