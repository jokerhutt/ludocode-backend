package com.ludocode.ludocodebackend.playground.api.dto.request

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import java.util.UUID

data class CreateProjectRequest(
    val projectName: String,
    val projectLanguage: LanguageType,
    val requestHash: UUID
)
