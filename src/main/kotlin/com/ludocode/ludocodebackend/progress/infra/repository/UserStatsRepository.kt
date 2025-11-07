package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserStatsRepository : JpaRepository<UserStats, UUID> {

    @Modifying
    @Query(
        """
  INSERT INTO user_stats(user_id, coins, streak)
  VALUES (:userId, 0, 0)
  ON CONFLICT (user_id) DO NOTHING
  """,
        nativeQuery = true
    )
    fun upsertUserStats(@Param("userId") userId: UUID): Int



}