package com.ludocode.ludocodebackend.projects.api.dto.response

import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import java.time.OffsetDateTime
import java.util.UUID


data class ProjectCardResponse (
    val projectId: UUID,
    val authorId: UUID,
    val projectTitle: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val visibility: Visibility,
    val languageName: String,
    val languageIconName: String
)

data class ProjectCardListResponse (
    val projects: List<ProjectCardResponse>,
    val page: Int,
    val totalPages: Int,
    val hasNext: Boolean
)