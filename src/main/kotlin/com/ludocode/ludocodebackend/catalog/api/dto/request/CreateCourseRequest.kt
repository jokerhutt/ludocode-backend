package com.ludocode.ludocodebackend.catalog.api.dto.request

import java.util.UUID

data class CreateCourseRequest(val courseTitle: String, val requestHash: UUID)
