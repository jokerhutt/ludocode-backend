package com.ludocode.ludocodebackend.projects.api.dto.request

import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import java.util.*

data class CreateProjectRequest(
    val projectName: String,
    val projectType: ProjectType,
    val files: List<ProjectFileSnapshot>,
    val entryFilePath: String,
    val requestHash: UUID
)

