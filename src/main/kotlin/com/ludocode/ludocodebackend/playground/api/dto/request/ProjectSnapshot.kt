package com.ludocode.ludocodebackend.playground.api.dto.request

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import java.time.OffsetDateTime
import java.util.UUID

data class ProjectSnapshot(
   val projectId: UUID,
   val projectName: String,
   val projectLanguage: LanguageType,
   val updatedAt: OffsetDateTime?,
   val files: List<ProjectFileSnapshot>,
)
