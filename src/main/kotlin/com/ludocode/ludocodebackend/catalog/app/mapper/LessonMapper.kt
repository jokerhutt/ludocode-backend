package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.projection.UserLessonProjection
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import org.springframework.stereotype.Component


@Component
class LessonMapper(private val basicMapper: BasicMapper) {

    fun toLessonResponse(p: UserLessonProjection): LessonResponse =
        LessonResponse(
            id = p.getId(),
            title = p.getTitle(),
            orderIndex = p.getOrderIndex(),
            isCompleted = p.getIsCompleted()
        )

    fun toLessonResponseList(rows: List<UserLessonProjection>): List<LessonResponse> =
        rows.map(::toLessonResponse)
}