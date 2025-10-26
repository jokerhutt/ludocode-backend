package com.ludocode.ludocodebackend.catalog.api.dto.admin.response

import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Module

data class CCModuleResponse(
    val module: Module,
    val lessons: List<CCLessonResponse>
)
