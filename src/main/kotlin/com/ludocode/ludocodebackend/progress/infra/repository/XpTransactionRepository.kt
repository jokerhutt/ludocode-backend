package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.XpTransaction
import com.ludocode.ludocodebackend.progress.infra.projection.LeaderboardRowProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.UUID

interface XpTransactionRepository : JpaRepository<XpTransaction, UUID> {

    fun existsByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        userId: UUID,
        start: OffsetDateTime,
        end: OffsetDateTime
    ): Boolean

    fun findByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
        userId: UUID,
        cutoff: OffsetDateTime
    ): List<XpTransaction>
    @Query("""
    SELECT
        u.id as userId,
        u.displayName as displayName,
        u.avatarVersion as avatarVersion,
        u.avatarIndex as avatarIndex,
        COALESCE(SUM(xt.amount), 0) as xp
    FROM XpTransaction xt
    JOIN User u ON u.id = xt.userId
    WHERE xt.createdAt >= :start
      AND xt.createdAt < :end
    GROUP BY
        u.id,
        u.displayName,
        u.avatarVersion,
        u.avatarIndex
    ORDER BY 
        SUM(xt.amount) DESC,
        MAX(xt.createdAt) DESC
    """)
    fun findWeeklyLeaderboard(
        start: OffsetDateTime,
        end: OffsetDateTime
    ): List<LeaderboardRowProjection>

}

