package com.ludocode.ludocodebackend.progress.domain.entity

import com.ludocode.ludocodebackend.progress.domain.entity.embedded.UserDailyGoalId
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "user_daily_goal")
class UserDailyGoal (

    @EmbeddedId
    val userDailyGoalId: UserDailyGoalId

)