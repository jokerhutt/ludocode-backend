package com.ludocode.ludocodebackend.projects.api.dto.request

import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import java.util.*

data class CreateProjectRequest(
    val projectName: String,
    val projectLanguageId: Long,
    val projectType: ProjectType,
    val requestHash: UUID
)

