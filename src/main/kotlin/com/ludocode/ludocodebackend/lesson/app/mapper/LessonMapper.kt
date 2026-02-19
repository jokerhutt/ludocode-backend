package com.ludocode.ludocodebackend.lesson.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.lesson.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.lesson.infra.repository.UserLessonProjection
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