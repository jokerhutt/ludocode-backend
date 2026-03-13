package com.ludocode.ludocodebackend.lesson.app.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.lesson.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.lesson.infra.repository.UserLessonProjection
import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import org.springframework.stereotype.Component

@Component
class LessonMapper(
    private val basicMapper: BasicMapper,
    private val objectMapper: ObjectMapper
) {

    fun toLessonResponse(p: UserLessonProjection): LessonResponse =
        LessonResponse(
            id = p.getId(),
            title = p.getTitle(),
            orderIndex = p.getOrderIndex(),
            isCompleted = p.getIsCompleted(),
            projectSnapshot =
                p.getProjectSnapshot()
                    ?.let { objectMapper.readValue(it, ProjectSnapshot::class.java) }
        )

    fun toLessonResponseList(rows: List<UserLessonProjection>): List<LessonResponse> =
        rows.map(::toLessonResponse)
}