package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.projection.ModuleLessonProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ModuleRepository : JpaRepository<Module, UUID> {

    fun findAllByCourseId(courseId: UUID): List<Module>

    @Query(
        value = """
        SELECT 
          m.course_id   AS courseId, m.id AS moduleId, m.title AS moduleTitle, m.order_index AS moduleOrder,
          l.id AS lessonId, l.title AS lessonTitle, l.order_index AS lessonOrder,
          COALESCE((
            SELECT TRUE FROM lesson_completion lc
            WHERE lc.lesson_id = l.id AND lc.user_id = :userId
            LIMIT 1
          ), FALSE)     
          AS isCompleted
        FROM module m
        LEFT JOIN lesson l ON l.module_id = m.id
        WHERE m.course_id = :courseId
        ORDER BY m.order_index, l.order_index
    """,
        nativeQuery = true
    )
    fun findCourseTree(
        @Param("courseId") courseId: UUID,
        @Param("userId") userId: UUID
    ): List<ModuleLessonProjection>


}