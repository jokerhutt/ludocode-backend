package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.projection.FlatModuleLessonRow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ModuleRepository : JpaRepository<Module, UUID> {

    fun findAllByIdIn(ids: List<UUID>): List<Module>

    @Query(
        value = """
      SELECT 
        m.id          AS moduleId,
        m.order_index AS moduleOrder,
        l.id          AS lessonId,
        l.order_index AS lessonOrder
      FROM module m
      LEFT JOIN lesson l ON l.module_id = m.id
      WHERE m.course_id = :courseId
      ORDER BY m.order_index, l.order_index
    """,
        nativeQuery = true
    )
    fun findFlatCourseTree(@Param("courseId") courseId: UUID): List<FlatModuleLessonRow>


}