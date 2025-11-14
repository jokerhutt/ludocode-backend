package com.ludocode.ludocodebackend.playground.app.dto.request

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import java.util.UUID

data class ProjectSnapshot(
   val projectId: UUID,
   val projectName: String,
   val projectLanguage: LanguageType,
   val files: List<ProjectFileSnapshot>
)
