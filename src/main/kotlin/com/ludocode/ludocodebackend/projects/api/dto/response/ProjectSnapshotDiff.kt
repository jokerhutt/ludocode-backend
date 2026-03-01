package com.ludocode.ludocodebackend.projects.api.dto.response

import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectFileSnapshot
import com.ludocode.ludocodebackend.projects.domain.entity.ProjectFile

data class ProjectSnapshotDiff(
    val toAdd: List<ProjectFileSnapshot>,
    val toDeleteFiles: List<ProjectFile>,
    val toUpdate: List<ProjectFileSnapshot>
)