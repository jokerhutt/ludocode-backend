package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.LessonResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleNodeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ModuleResponse
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.infra.projection.ModuleLessonProjection
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import org.springframework.stereotype.Component

@Component
class CourseTreeMapper (private val basicMapper: BasicMapper) {

    fun toCourseTree(course: Course, rows: List<ModuleLessonProjection>): CourseTreeResponse {
        val byModule = rows.groupBy { it.getModuleId() }

        val modules = byModule.entries
            .sortedBy { it.value.first().getModuleOrder() }
            .map { (_, group) -> toModuleNodeResponse(group)}

        return CourseTreeResponse(
            id = requireNotNull(course.id),
            title = requireNotNull(course.title),
            modules = modules
        )
    }

    private fun toModuleNodeResponse(group: List<ModuleLessonProjection>): ModuleNodeResponse {
        val head = group.first()
        return ModuleNodeResponse(
            module = ModuleResponse(
                id = head.getModuleId(),
                title = head.getModuleTitle(),
                courseId = head.getCourseId(),
                orderIndex = head.getModuleOrder()
            ),
            lessons = group
                .filter { it.getLessonId() != null }
                .sortedBy { it.getLessonOrder() }
                .map { toLessonResponse(it) }
        )
    }

    private fun toLessonResponse(row: ModuleLessonProjection): LessonResponse {
        return LessonResponse(
            id = row.getLessonId()!!,
            title = row.getLessonTitle()!!,
            orderIndex = row.getLessonOrder()!!,
            isCompleted = row.getIsCompleted()
        )
    }

}