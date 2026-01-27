package com.ludocode.ludocodebackend.playground.api.dto.response

import com.ludocode.ludocodebackend.playground.api.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile

data class ProjectSnapshotDiff(
    val toAdd: List<ProjectFileSnapshot>,
    val toDeleteFiles: List<ProjectFile>,
    val toUpdate: List<ProjectFileSnapshot>
)