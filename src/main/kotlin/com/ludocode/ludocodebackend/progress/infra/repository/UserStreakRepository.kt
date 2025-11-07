package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

interface UserStreakRepository : JpaRepository<UserStreak, UUID> {


    @Modifying
    @Query( value =  """
        INSERT INTO user_streak (user_id)
        VALUES (:userId)
        ON CONFLICT (user_id) DO NOTHING
        """, nativeQuery = true)
    fun initializeIfAbsent(@Param("userId") userId: UUID)

    @Modifying
    @Query(
        value = """
      INSERT INTO user_streak (user_id, current_streak_days, best_streak_days, last_met_local_date, last_met_goal_utc)
      VALUES (:userId, :current, :best, :lastLocal, :lastUtc)
      ON CONFLICT (user_id) DO UPDATE SET
        current_streak_days = EXCLUDED.current_streak_days,
        best_streak_days    = GREATEST(user_streak.best_streak_days, EXCLUDED.best_streak_days),
        last_met_local_date = EXCLUDED.last_met_local_date,
        last_met_goal_utc   = EXCLUDED.last_met_goal_utc
    """,
        nativeQuery = true
    )
    fun upsertProgress(
        @Param("userId") userId: UUID,
        @Param("current") current: Int,
        @Param("best") best: Int,
        @Param("lastLocal") lastLocal: LocalDate,
        @Param("lastUtc") lastUtc: OffsetDateTime
    ): Int

    fun findByUserId(userId: UUID): UserStreak?


    //lock prevents writes while in use
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT us FROM UserStreak us WHERE us.userId = :userId")
    fun getForUpdate(@Param("userId") userId: UUID): UserStreak?




}