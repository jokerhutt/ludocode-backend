package com.ludocode.ludocodebackend.lesson.infra.repository

import com.ludocode.ludocodebackend.lesson.domain.entity.Lesson
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface LessonRepository : JpaRepository<Lesson, UUID> {

    @Query(
        value = """
    SELECT l.id AS id,
           l.title AS title,
           ml.order_index AS orderIndex,
           l.project_snapshot::text AS project_snapshot,
           l.lesson_type AS lessonType,
           EXISTS (
             SELECT 1 FROM lesson_completion lc
             WHERE lc.is_deleted = false
               AND lc.lesson_id = l.id
               AND lc.user_id   = :userId
           ) AS isCompleted
    FROM lesson l
    JOIN module_lessons ml ON ml.lesson_id = l.id
    WHERE l.is_deleted = false
      AND l.id = :lessonId
  """, nativeQuery = true
    )
    fun findUserLesson(
        @Param("lessonId") lessonId: UUID,
        @Param("userId") userId: UUID
    ): UserLessonProjection?


    @Query(
        value = """
    SELECT 
      l.id           AS id,
      l.title        AS title,
      l.project_snapshot::text AS project_snapshot,
      ml.order_index AS orderIndex,
      EXISTS (
        SELECT 1 FROM lesson_completion lc
        WHERE lc.is_deleted = false
          AND lc.lesson_id = l.id
          AND lc.user_id   = :userId
      ) AS isCompleted
    FROM lesson l
    JOIN module_lessons ml ON ml.lesson_id = l.id
    WHERE l.id IN (:lessonIds)
      AND l.is_deleted = false
    ORDER BY ml.order_index
  """,
        nativeQuery = true
    )
    fun findUserLessonsByIds(
        @Param("lessonIds") lessonIds: List<UUID>,
        @Param("userId") userId: UUID
    ): List<UserLessonProjection>

    @Query(value = "SELECT module_id FROM module_lessons WHERE lesson_id = :lessonId LIMIT 1", nativeQuery = true)
    fun findModuleIdForLesson(@Param("lessonId") lessonId: UUID): UUID?

    @Query(
        value = """
    SELECT m.id
    FROM module m
    WHERE m.course_id = :courseId
      AND m.is_deleted = false
    ORDER BY m.order_index
    LIMIT 1
  """,
        nativeQuery = true
    )
    fun findFirstModuleIdInCourse(
        @Param("courseId") courseId: UUID
    ): UUID?


    @Query(
        value = """
        SELECT *
        FROM lesson
        WHERE id = :id
        AND is_deleted = false
        """, nativeQuery = true
    )
    fun findActiveById(@Param("id") id: UUID): Lesson?


}