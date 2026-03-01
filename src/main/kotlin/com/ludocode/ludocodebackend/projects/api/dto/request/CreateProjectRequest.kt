package com.ludocode.ludocodebackend.projects.api.dto.request

import java.util.*

data class CreateProjectRequest(
    val projectName: String,
    val projectLanguageId: Long,
    val requestHash: UUID
)

