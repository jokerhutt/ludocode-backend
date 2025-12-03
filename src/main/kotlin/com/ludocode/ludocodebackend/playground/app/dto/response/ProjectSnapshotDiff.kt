package com.ludocode.ludocodebackend.playground.app.dto.response

import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import java.util.UUID

data class ProjectSnapshotDiff(
    val toAdd: List<ProjectFileSnapshot>,
    val toDeleteFiles: List<ProjectFile>,
    val toUpdate: List<ProjectFileSnapshot>
)