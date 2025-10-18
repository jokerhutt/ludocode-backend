package com.ludocode.ludocodebackend.catalog.api.dto.response

data class ModuleNodeResponse(
   val module: ModuleResponse,
   val lessons: List<LessonResponse>
)
