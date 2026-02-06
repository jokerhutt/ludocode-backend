package com.ludocode.ludocodebackend.preferences.api.infra.repository

import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserPreferencesRepository : JpaRepository<UserPreferences, UUID> {

}