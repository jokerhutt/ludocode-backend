package com.ludocode.ludocodebackend.projects.infra.repository

import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import com.ludocode.ludocodebackend.projects.infra.projection.ProjectCardProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface UserProjectRepository : JpaRepository<UserProject, UUID> {


    @Query(
        value = """
    SELECT 
        p.id as projectId,
        p.userId as authorId,
        p.name as projectTitle,
        p.createdAt as createdAt,
        p.updatedAt as updatedAt,
        p.deleteAt as deleteAt,
        p.projectVisibility as visibility,
        p.projectType as projectType
    FROM UserProject p
    WHERE p.userId = :userId
    ORDER BY p.updatedAt DESC
    """,
        countQuery = """
    SELECT COUNT(p)
    FROM UserProject p
    WHERE p.userId = :userId
    """
    )
    fun findProjectCardsByUserId(
        @Param("userId") userId: UUID,
        pageable: Pageable
    ): Page<ProjectCardProjection>

    @Query(
        value = """
        SELECT 
            p.id as projectId,
            p.userId as authorId,
            p.name as projectTitle,
            p.createdAt as createdAt,
            p.deleteAt as deleteAt,
            p.updatedAt as updatedAt,
            p.projectVisibility as visibility,
            p.projectType as projectType
        FROM UserProject p
        WHERE p.projectVisibility = 'PUBLIC'
          AND p.deleteAt IS NULL
        ORDER BY p.createdAt DESC
    """,
        countQuery = """
        SELECT COUNT(p)
        FROM UserProject p
        WHERE p.projectVisibility = 'PUBLIC'
          AND p.deleteAt IS NULL
    """
    )
    fun findPublicProjectCards(pageable: Pageable): Page<ProjectCardProjection>

    @Query(
        """
    SELECT p
    FROM UserProject p
    WHERE p.deleteAt IS NOT NULL
      AND p.deleteAt <= :now
    """
    )
    fun findAllReadyForDeletion(
        @Param("now") now: OffsetDateTime
    ): List<UserProject>

    fun findAllByUserIdOrderByUpdatedAtDesc(userId: UUID): List<UserProject>

    fun countByUserId(userId: UUID): Long

}