package com.ludocode.ludocodebackend.playground.api.dto.request

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import java.util.UUID

data class CreateProjectRequest(
    val projectName: String,
    val projectLanguageId: Long,
    val requestHash: UUID
)

data class CreateProjectLanguageRequest(
    val slug: String,
    val name: String,
    val editorId: String
)
