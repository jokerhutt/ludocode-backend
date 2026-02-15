package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLesson
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ModuleLessonsRepository : JpaRepository<ModuleLesson, ModuleLessonsId> {

    @Query(value = """
        SELECT lesson.id
        FROM lesson
        JOIN module_lessons ON module_lessons.lesson_id = lesson.id
        WHERE module_lessons.module_id = :moduleId
          AND lesson.is_deleted = false
        """, nativeQuery = true)
    fun findActiveLessonIdsByModuleId (@Param("moduleId") moduleId: UUID) : List<UUID>

    @Query(value = """
        SELECT *
        FROM lesson
        JOIN module_lessons ON module_lessons.lesson_id = lesson.id
        WHERE module_lessons.module_id = :moduleId
          AND lesson.is_deleted = false
        """, nativeQuery = true)
    fun findActiveLessonsByModuleId (@Param("moduleId") moduleId: UUID) : List<Lesson>

    fun deleteByModuleLessonsIdModuleId(moduleId: UUID)

    @Query(value = """
        SELECT module_lessons.order_index
        FROM module_lessons
        WHERE module_id = :moduleId
        AND lesson_id = :lessonId
        """, nativeQuery = true)
    fun findOrderIndexForLesson (@Param("moduleId") moduleId: UUID, @Param("lessonId") lessonId: UUID) : Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
  DELETE FROM ModuleLesson ml
  WHERE ml.moduleLessonsId.moduleId = :moduleId
"""
    )
    fun deleteLessonsInModule(@Param("moduleId") moduleId: UUID)

}