package com.ludocode.ludocodebackend.catalog.api.dto.request

import com.ludocode.ludocodebackend.catalog.domain.enums.CourseStatus

data class CourseStatusRequest (
    val value: CourseStatus,
)