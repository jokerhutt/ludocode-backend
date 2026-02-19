package com.ludocode.ludocodebackend.playground.api.dto.request

import java.util.*

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
