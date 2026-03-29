package com.ludocode.ludocodebackend.projects.infra.repository

import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile
import com.ludocode.ludocodebackend.projects.infra.projection.ProjectFileLanguageProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface ProjectFileRepository : JpaRepository<ProjectFile, UUID> {
    @Query("SELECT * FROM project_file WHERE project_id = :projectId", nativeQuery = true)
    fun findAllProjectFilesByProjectId(@Param("projectId") projectId: UUID): List<ProjectFile>

    @Query(
        """
        SELECT DISTINCT pf.projectId as projectId, pf.codeLanguage as codeLanguage
        FROM ProjectFile pf
        WHERE pf.projectId IN :projectIds
        """
    )
    fun findDistinctLanguagesByProjectIdIn(
        @Param("projectIds") projectIds: List<UUID>
    ): List<ProjectFileLanguageProjection>

}