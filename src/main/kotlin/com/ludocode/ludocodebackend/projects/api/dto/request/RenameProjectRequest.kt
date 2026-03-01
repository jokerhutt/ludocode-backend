package com.ludocode.ludocodebackend.projects.api.dto.request

import java.util.*

data class RenameProjectRequest(val targetId: UUID, val newName: String)