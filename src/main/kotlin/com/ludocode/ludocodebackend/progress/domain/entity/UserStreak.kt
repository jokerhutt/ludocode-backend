package com.ludocode.ludocodebackend.progress.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnDefault
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "user_streak")
class UserStreak (

    @Id
    val userId: UUID,

    @Column(name = "current_streak_days")
    @ColumnDefault("0")
    var currentStreakDays: Int? = 0,

    @Column(name = "best_streak_days")
    @ColumnDefault("0")
    var bestStreakDays: Int? = 0,

    @Column(name = "last_met_local_date")
    var lastMetLocalDate: LocalDate? = null,

    @Column(name = "last_met_goal_utc")
    var lastMetGoalUtc: OffsetDateTime? = null

)