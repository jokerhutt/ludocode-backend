package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatLesson
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatModule
import com.ludocode.ludocodebackend.catalog.infra.projection.FlatModuleLessonRow
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FlatCourseTreeMapper {

    fun toFlatTree(courseId: UUID, rows: List<FlatModuleLessonRow>): FlatCourseTreeResponse {
        val modules = rows
            .groupBy { it.getModuleId() }
            .entries
            .sortedBy { it.value.first().getModuleOrder() }
            .map { (_, group) -> mapFlatModule(group) }

        return FlatCourseTreeResponse(courseId = courseId, modules = modules)
    }

    private fun mapFlatModule(group: List<FlatModuleLessonRow>): FlatModule {
        val head = group.first()
        return FlatModule(
            id = head.getModuleId(),
            orderIndex = head.getModuleOrder(),
            lessons = group
                .filter { it.getLessonId() != null }
                .sortedBy { it.getLessonOrder() }
                .map { mapFlatLesson(it) }
        )
    }

    private fun mapFlatLesson(row: FlatModuleLessonRow): FlatLesson =
        FlatLesson(
            id = row.getLessonId()!!,
            orderIndex = row.getLessonOrder()!!
        )
}