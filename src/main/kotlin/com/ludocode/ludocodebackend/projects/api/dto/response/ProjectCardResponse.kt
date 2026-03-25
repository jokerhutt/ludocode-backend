package com.ludocode.ludocodebackend.projects.api.dto.response

import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import java.time.OffsetDateTime
import java.util.UUID


data class ProjectCardResponse (
    val projectId: UUID,
    val authorId: UUID,
    val projectTitle: String,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val visibility: Visibility,
    val projectType: ProjectType,
    val languageName: String,
    val languageIconName: String,
    val deleteAt: OffsetDateTime?,
)

data class ProjectCardListResponse (
    val projects: List<ProjectCardResponse>,
    val page: Int,
    val totalPages: Int,
    val hasNext: Boolean
)