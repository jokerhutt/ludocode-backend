package com.ludocode.ludocodebackend.preferences.infra.repository

import com.ludocode.ludocodebackend.preferences.domain.entity.UserPreferences
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserPreferencesRepository : JpaRepository<UserPreferences, UUID>