package com.ludocode.ludocodebackend.playground.app.dto.request

import java.util.UUID

data class ProjectSnapshot(
   val projectId: UUID,
   val files: ProjectFileSnapshot
)
