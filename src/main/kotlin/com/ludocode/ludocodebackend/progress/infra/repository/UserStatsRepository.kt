package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserStats
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserStatsRepository : JpaRepository<UserStats, UUID> {



}