package com.ludocode.ludocodebackend.user.infra.repository

import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import com.ludocode.ludocodebackend.user.domain.enums.DesiredPath
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserPreferencesRepository : JpaRepository<UserPreferences, UUID> {

}