package com.ludocode.ludocodebackend.ai.infra.repository

import com.ludocode.ludocodebackend.ai.domain.entity.UserAICredits
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserAICreditsRepository : JpaRepository<UserAICredits, UUID>