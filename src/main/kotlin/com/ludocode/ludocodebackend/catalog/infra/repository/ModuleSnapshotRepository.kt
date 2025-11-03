package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ModuleSnapshotRepository : JpaRepository<Module, UUID> {

    @Query(value = """
        SELECT id
        FROM module
        WHERE course_id = :courseId
        AND is_deleted = false
        """, nativeQuery = true)
    fun findAllActiveByCourseId (@Param("courseId") courseId: UUID) : List<UUID>

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

    @Query(value = """
        SELECT *
        FROM module
        WHERE id = :moduleId
        AND is_deleted = false
        """, nativeQuery = true)
    fun findActiveById(@Param("moduleId") moduleId: UUID): Module?


}