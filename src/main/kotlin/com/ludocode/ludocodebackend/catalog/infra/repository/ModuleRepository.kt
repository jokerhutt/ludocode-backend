package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.projection.FlatModuleLessonRow
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional
import java.util.UUID

interface ModuleRepository : JpaRepository<Module, UUID> {

    @Query("""
    SELECT * 
    FROM module 
    WHERE id IN (:ids)
      AND is_deleted = false
""", nativeQuery = true)
    fun findAllByIdIn(@Param("ids") ids: List<UUID>): List<Module>

    @Query(value = """
        SELECT *
        FROM module
        WHERE id = :moduleId
        AND is_deleted = false
        """, nativeQuery = true)
    fun findActiveById(@Param("moduleId") moduleId: UUID): Module?

    @Query(value = """
  SELECT
    m.id            AS moduleId,
    m.order_index   AS moduleOrder,
    ml.lesson_id    AS lessonId,
    ml.order_index  AS lessonOrder
  FROM module m
  JOIN module_lessons ml
    ON ml.module_id = m.id
  JOIN lesson l
    ON l.id = ml.lesson_id
  WHERE m.course_id = :courseId
    AND m.is_deleted = false
    AND l.is_deleted = false
  ORDER BY m.order_index, ml.order_index, l.id
""", nativeQuery = true)
    fun findFlatCourseTree(@Param("courseId") courseId: UUID): List<FlatModuleLessonRow>

    @Query(value = """
        SELECT id
        FROM module
        WHERE course_id = :courseId
        AND is_deleted = false
        """, nativeQuery = true)
    fun findActiveIdsByCourse(@Param("courseId") courseId: UUID): List<UUID>



    @Modifying
    @Query(value = """
        UPDATE module
        SET is_deleted = true
        WHERE id IN (:ids)
        """, nativeQuery = true)
    fun softDeleteModulesByModuleIds (@Param("ids") ids: List<UUID>): Int

    @Modifying
    @Query(value = """
        UPDATE module
        SET order_index = order_index + 1000
        WHERE course_id = :courseId
        AND is_deleted = false
        """, nativeQuery = true)
    fun bumpAllModuleOrderIndexesInCourse (@Param("courseId") courseId: UUID)




}