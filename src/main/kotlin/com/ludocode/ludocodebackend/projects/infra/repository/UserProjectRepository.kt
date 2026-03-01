package com.ludocode.ludocodebackend.projects.infra.repository

import com.ludocode.ludocodebackend.projects.domain.entity.UserProject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

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