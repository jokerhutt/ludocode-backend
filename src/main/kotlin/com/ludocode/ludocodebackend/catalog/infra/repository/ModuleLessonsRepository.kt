package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLessons
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ModuleLessonsRepository : JpaRepository<ModuleLessons, ModuleLessonsId> {

    @Query(value = """
        SELECT lesson.id
        FROM lesson
        JOIN module_lessons ON module_lessons.lesson_id = lesson.id
        WHERE module_lessons.module_id = :moduleId
          AND lesson.is_deleted = false
        """, nativeQuery = true)
    fun findActiveLessonsByModuleId (@Param("moduleId") moduleId: UUID) : List<UUID>

    @Modifying
    @Query(value = """
        DELETE
        FROM module_lessons
        WHERE module_id = :moduleId
        """, nativeQuery = true)
    fun deleteLessonsInModule (@Param("moduleId") moduleId: UUID)

}