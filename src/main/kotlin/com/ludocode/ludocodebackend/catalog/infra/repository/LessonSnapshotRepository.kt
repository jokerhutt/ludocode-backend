package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LessonSnapshotRepository : JpaRepository<Lesson, UUID> {

    @Modifying
    @Query(value = """
        UPDATE lesson
        SET is_deleted = true
        WHERE id IN (:ids)
        """, nativeQuery = true)
    fun softDeleteLessonsByIds (@Param("ids") ids: List<UUID>): Int

    @Query(value = """
        SELECT *
        FROM lesson
        WHERE is_deleted = false
        """, nativeQuery = true)
    fun findActiveById (@Param("id") id: UUID) : Lesson?


}