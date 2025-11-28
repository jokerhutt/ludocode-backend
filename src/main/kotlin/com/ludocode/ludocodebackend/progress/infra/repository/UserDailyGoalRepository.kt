package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserDailyGoal
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.UserDailyGoalId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface UserDailyGoalRepository : JpaRepository<UserDailyGoal, UserDailyGoalId> {


    @Modifying
    @Query(
        value = """
      INSERT INTO user_daily_goal (user_id, local_date)
      VALUES (:userId, :localDate)
      ON CONFLICT (user_id, local_date) DO NOTHING
    """,
        nativeQuery = true
    )
    fun insertOnce(
        @Param("userId") userId: UUID,
        @Param("localDate") localDate: LocalDate
    ): Int

    @Query(
        value = """
        SELECT *
        FROM user_daily_goal
        WHERE user_id = :userId
        ORDER BY local_date DESC
        LIMIT :limit
    """,
        nativeQuery = true
    )
    fun findRecentCompletions(
        userId: UUID,
        limit: Int
    ): List<UserDailyGoal>

}