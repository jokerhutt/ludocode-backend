package com.ludocode.ludocodebackend.playground.api.dto.request

import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import java.time.OffsetDateTime
import java.util.*

data class ProjectSnapshot(
    val projectId: UUID,
    val projectName: String,
    val projectLanguage: LanguageMetadata,
    val updatedAt: OffsetDateTime?,
    val deleteAt: OffsetDateTime?,
    val files: List<ProjectFileSnapshot>,
)
