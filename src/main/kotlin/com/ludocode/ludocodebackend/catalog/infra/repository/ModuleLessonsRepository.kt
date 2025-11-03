package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
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
    fun findActiveLessonIdsByModuleId (@Param("moduleId") moduleId: UUID) : List<UUID>

    @Query(value = """
        SELECT lesson
        FROM lesson
        JOIN module_lessons ON module_lessons.lesson_id = lesson.id
        WHERE module_lessons.module_id = :moduleId
          AND lesson.is_deleted = false
        """, nativeQuery = true)
    fun findActiveLessonsByModuleId (@Param("moduleId") moduleId: UUID) : List<Lesson>

    @Query(value = """
        SELECT module_lessons.order_index
        FROM module_lessons
        WHERE module_id = :moduleId
        AND lesson_id = :lessonId
        """, nativeQuery = true)
    fun findOrderIndexForLesson (@Param("moduleId") moduleId: UUID, @Param("lessonId") lessonId: UUID) : Int

    @Modifying
    @Query(value = """
        DELETE
        FROM module_lessons
        WHERE module_id = :moduleId
        """, nativeQuery = true)
    fun deleteLessonsInModule (@Param("moduleId") moduleId: UUID)

    @Query(value = """
        SELECT module_id
        FROM module_lessons
        WHERE lesson_id = :lessonId
        """, nativeQuery = true)
    fun findModuleIdForLesson(@Param("lessonId") lessonId: UUID): UUID?

}