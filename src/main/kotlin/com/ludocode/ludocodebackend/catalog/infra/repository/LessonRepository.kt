package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.infra.projection.LessonIdTreeProjection
import com.ludocode.ludocodebackend.catalog.infra.projection.UserLessonProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

import java.util.UUID

interface LessonRepository : JpaRepository<Lesson, UUID> {

    @Query(value = """
    SELECT l.id AS id,
           l.title AS title,
           ml.order_index AS orderIndex,
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
  """, nativeQuery = true)
    fun findUserLesson(
        @Param("lessonId") lessonId: UUID,
        @Param("userId") userId: UUID
    ): UserLessonProjection?


    @Query(
        value = """
    SELECT 
      l.id           AS id,
      l.title        AS title,
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

    @Query(value = """
    WITH ordered AS (
      SELECT l.id AS lesson_id,
             LEAD(l.id) OVER (
               PARTITION BY m.course_id
               ORDER BY m.order_index, ml.order_index, l.id
             ) AS next_id
      FROM module m
      JOIN module_lessons ml ON ml.module_id = m.id
      JOIN lesson l ON l.id = ml.lesson_id
      WHERE m.is_deleted = false
        AND l.is_deleted = false
    )
    SELECT next_id
    FROM ordered
    WHERE lesson_id = :currentLesson
    """, nativeQuery = true)
    fun findNextLessonId(@Param("currentLesson") currentLesson: UUID): UUID?

    @Query(value = """
    SELECT m.course_id
    FROM module_lessons ml
    JOIN module m ON m.id = ml.module_id
    WHERE ml.lesson_id = :lessonId
    LIMIT 1
    """, nativeQuery = true)
    fun findCourseIdByLesson(@Param("lessonId") lessonId: UUID): UUID?

    @Query(value = """
    SELECT l.id
    FROM module m
    JOIN module_lessons ml ON ml.module_id = m.id
    JOIN lesson l ON l.id = ml.lesson_id
    WHERE m.course_id = :courseId
      AND m.is_deleted = false
      AND l.is_deleted = false
    ORDER BY m.order_index, ml.order_index, l.id
    LIMIT 1
  """, nativeQuery = true)
    fun findFirstLessonIdInCourse(@Param("courseId") courseId: UUID): UUID?

    @Query(value = """
    WITH ordered AS (
      SELECT l.id AS lessonId, m.id AS moduleId, m.course_id AS courseId,
             LEAD(l.id) OVER (
               PARTITION BY m.course_id
               ORDER BY m.order_index, ml.order_index, l.id
             ) AS nextLessonId
      FROM module m
      JOIN module_lessons ml ON ml.module_id = m.id
      JOIN lesson l ON l.id = ml.lesson_id
      WHERE m.is_deleted = false
        AND l.is_deleted = false
    )
    SELECT lessonId, moduleId, courseId, nextLessonId
    FROM ordered
    WHERE lessonId = :lessonId
  """, nativeQuery = true)
    fun findLessonIdTree(@Param("lessonId") lessonId: UUID): LessonIdTreeProjection?

    @Modifying
    @Query(value = """
        UPDATE lesson
        SET is_deleted = true
        WHERE id = :id
        """, nativeQuery = true)
    fun softDeleteLessonById (@Param("id") id: UUID): Int

    @Query(value = """
        SELECT *
        FROM lesson
        WHERE id = :id
        AND is_deleted = false
        """, nativeQuery = true)
    fun findActiveById (@Param("id") id: UUID) : Lesson?


}