package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserStreak
import com.ludocode.ludocodebackend.progress.infra.projection.UserStreakRow
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

    fun findByUserId(userId: UUID) : UserStreak?

}