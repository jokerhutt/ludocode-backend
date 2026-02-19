package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.UserCoins
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserCoinsRepository : JpaRepository<UserCoins, UUID>