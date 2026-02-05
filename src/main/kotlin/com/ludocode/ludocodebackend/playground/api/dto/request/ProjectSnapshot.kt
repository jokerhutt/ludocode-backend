package com.ludocode.ludocodebackend.playground.api.dto.request

import com.ludocode.ludocodebackend.playground.domain.entity.CodeLanguages
import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import java.time.OffsetDateTime
import java.util.UUID

data class ProjectSnapshot(
   val projectId: UUID,
   val projectName: String,
   val projectLanguage: LanguageMetadata,
   val updatedAt: OffsetDateTime?,
   val files: List<ProjectFileSnapshot>,
)

data class LanguageMetadata(
   val languageId: Long,
   val name: String,
   val slug: String,
   val editorId: String,
   val pistonId: String,
   val extension: String,
   val base: String,
   val iconName: String,
   val initialScript: String,

)