package com.ludocode.ludocodebackend.projects.api.dto.snapshot
import java.time.OffsetDateTime
import java.util.UUID

data class ProjectSnapshot(
    val projectId: UUID,
    val projectName: String,
    val filesUrl: String?,
    val updatedAt: OffsetDateTime?,
    val deleteAt: OffsetDateTime?,
    val files: List<ProjectFileSnapshot>,
    var entryFilePath: String
)