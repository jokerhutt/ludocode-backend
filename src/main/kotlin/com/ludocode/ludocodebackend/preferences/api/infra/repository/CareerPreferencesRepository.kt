package com.ludocode.ludocodebackend.preferences.api.infra.repository

import com.ludocode.ludocodebackend.preferences.domain.entity.CareerPreference
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CareerPreferencesRepository : JpaRepository<CareerPreference, UUID> {

    fun findByChoice(choice: String): CareerPreference?

}