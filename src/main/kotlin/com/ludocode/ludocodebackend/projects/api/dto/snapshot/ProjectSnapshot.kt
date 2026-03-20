package com.ludocode.ludocodebackend.projects.api.dto.snapshot

import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import java.time.OffsetDateTime
import java.util.UUID

data class ProjectSnapshot(
    val projectId: UUID,
    val projectName: String,
    val projectLanguage: LanguageMetadata,
    val updatedAt: OffsetDateTime?,
    val deleteAt: OffsetDateTime?,
    val files: List<ProjectFileSnapshot>,
    val entryFileId: UUID,
)