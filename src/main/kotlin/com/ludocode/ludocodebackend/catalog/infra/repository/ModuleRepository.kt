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

    @Query("""
    select m.id 
    from module m
    where m.course_id = :courseId
    and m.is_deleted = false
""", nativeQuery = true)
    fun findModuleIdsByCourse(@Param("courseId") courseId: UUID): List<UUID>


    @Query(
        value = """
      SELECT 
        m.id          AS moduleId,
        m.order_index AS moduleOrder,
        l.id          AS lessonId,
        l.order_index AS lessonOrder
      FROM module m
      LEFT JOIN exercise l 
        ON l.module_id = m.id 
       AND l.is_deleted = false
      WHERE m.course_id = :courseId
        AND m.is_deleted = false
      ORDER BY m.order_index, l.order_index
    """,
        nativeQuery = true
    )
    fun findFlatCourseTree(@Param("courseId") courseId: UUID): List<FlatModuleLessonRow>


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Module m where m.id = :id")
    fun findByIdForUpdate(@Param("id") id: UUID): Optional<Module>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
    UPDATE module 
    SET order_index = order_index + 100000
    WHERE course_id = :courseId AND is_deleted = false
  """,
        nativeQuery = true
    )
    fun bumpAllInCourse(@Param("courseId") courseId: UUID)

    @Modifying
    @Query("""
    UPDATE module
    SET is_deleted = true
    WHERE id IN (:ids)
""", nativeQuery = true)
    fun softDeleteIn(@Param("ids") ids: List<UUID>)

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = "UPDATE module SET order_index = :idx WHERE id = :id",
        nativeQuery = true
    )
    fun setOrder(@Param("id") id: UUID, @Param("idx") idx: Int)

    @Query(
        value = "SELECT id FROM module WHERE course_id = :courseId AND is_deleted = false ORDER BY order_index, id",
        nativeQuery = true
    )
    fun findActiveIdsByCourse(@Param("courseId") courseId: UUID): List<UUID>




}