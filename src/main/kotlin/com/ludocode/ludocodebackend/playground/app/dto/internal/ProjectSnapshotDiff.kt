package com.ludocode.ludocodebackend.playground.app.dto.internal

import com.ludocode.ludocodebackend.playground.app.dto.request.ProjectFileSnapshot
import com.ludocode.ludocodebackend.playground.domain.entity.ProjectFile
import java.util.UUID

data class ProjectSnapshotDiff(
    val remainingFileIds: List<UUID>,
    val toAdd: List<ProjectFileSnapshot>,
    val toDeleteFiles: List<ProjectFile>,
    val toUpdate: List<ProjectFileSnapshot>
)
