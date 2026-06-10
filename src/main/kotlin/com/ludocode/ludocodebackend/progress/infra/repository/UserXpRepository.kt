package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserXp
import com.ludocode.ludocodebackend.progress.infra.projection.LeaderboardRowProjection
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserXpRepository : JpaRepository<UserXp, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserXp u WHERE u.userId = :userId")
    fun findByUserIdForUpdate(userId: UUID): UserXp?

    @Query(
        value = """
        SELECT
            u.id AS userId,
            u.display_name AS displayName,
            u.avatar_version AS avatarVersion,
            u.avatar_index AS avatarIndex,
            ux.xp AS xp
        FROM user_xp ux
        JOIN ludo_user u
            ON u.id = ux.user_id
        WHERE u.is_deleted = false
        ORDER BY ux.xp DESC, u.id
    """,
        nativeQuery = true
    )
    fun findLeaderboard(
        pageable: Pageable
    ): Page<LeaderboardRowProjection>

}