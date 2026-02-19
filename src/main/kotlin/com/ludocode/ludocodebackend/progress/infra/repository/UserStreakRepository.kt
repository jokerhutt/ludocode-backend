package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserStreakRepository : JpaRepository<UserStreak, UUID> {

    fun findByUserId(userId: UUID): UserStreak?

}