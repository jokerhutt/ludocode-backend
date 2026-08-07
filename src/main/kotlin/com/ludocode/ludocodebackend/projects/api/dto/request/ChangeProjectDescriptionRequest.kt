package com.ludocode.ludocodebackend.projects.api.dto.request

import java.util.UUID

data class ChangeProjectDescriptionRequest(val targetId: UUID, val newDescription: String)
