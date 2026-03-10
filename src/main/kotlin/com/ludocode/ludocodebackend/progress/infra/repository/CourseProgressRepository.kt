package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.infra.projection.CourseLessonStatsProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.*

interface CourseProgressRepository : JpaRepository<CourseProgress, CourseProgressId> {

    @Query(
        value = """
        SELECT EXISTS(
            SELECT 1
            FROM course_progress
            WHERE user_id = :userId
        )
    """,
        nativeQuery = true
    )
    fun existsByUser(@Param("userId") userId: UUID): Boolean

    @Query(
        value = """
    select cp.course_id
    from course_progress cp
    where cp.user_id = :userId
    """,
        nativeQuery = true
    )
    fun findAllCourseIdsForUser(userId: UUID): List<UUID>

    @Query(
        value = """
  SELECT course_id
  FROM course_progress
  WHERE user_id = :userId
  ORDER BY updated_at DESC
  LIMIT 1
  """,
        nativeQuery = true
    )
    fun findCurrentCourseIdForUser(@Param("userId") userId: UUID): UUID?

    fun findByIdCourseId(courseId: UUID): List<CourseProgress>

    fun findByIdUserIdAndIdCourseIdIn(
        userId: UUID,
        courseIds: List<UUID>
    ): List<CourseProgress>

    fun existsByIdUserIdAndIdCourseId(
        userId: UUID,
        courseId: UUID
    ): Boolean

    @Query(
        value = """
        SELECT
            m.course_id                     AS courseId,
            COUNT(DISTINCT ml.lesson_id)     AS totalLessons,
            COUNT(DISTINCT lc.lesson_id)     AS completedLessons
        FROM module m
        JOIN module_lessons ml
            ON ml.module_id = m.id
        LEFT JOIN lesson_completion lc
            ON lc.lesson_id = ml.lesson_id
           AND lc.user_id = :userId
           AND lc.is_deleted = false
        WHERE m.course_id IN (:courseIds)
          AND m.is_deleted = false
        GROUP BY m.course_id
    """,
        nativeQuery = true
    )
    fun findCourseLessonStatsList(
        @Param("userId") userId: UUID,
        @Param("courseIds") courseIds: List<UUID>
    ): List<CourseLessonStatsProjection>

    @Modifying
    @Query(
        """
    UPDATE course_progress
    SET is_complete = false,
        updated_at = :now
    WHERE course_id = :courseId
    """,
        nativeQuery = true
    )
    fun markCourseIncompleteForAllUsers(
        @Param("courseId") courseId: UUID,
        @Param("now") now: OffsetDateTime
    ): Int

    @Query(
        value = """
        SELECT
            m.course_id                  AS courseId,
            COUNT(DISTINCT ml.lesson_id) AS totalLessons,
            COUNT(DISTINCT lc.lesson_id) AS completedLessons
        FROM module m
        JOIN module_lessons ml
            ON ml.module_id = m.id
        LEFT JOIN lesson_completion lc
            ON lc.lesson_id = ml.lesson_id
           AND lc.user_id = :userId
           AND lc.is_deleted = false
        WHERE m.course_id = :courseId
          AND m.is_deleted = false
        GROUP BY m.course_id
    """,
        nativeQuery = true
    )
    fun findSingleCourseStats(
        @Param("userId") userId: UUID,
        @Param("courseId") courseId: UUID
    ): CourseLessonStatsProjection?


    @Modifying
    @Query(
        """
    INSERT INTO course_progress (
      user_id, course_id, current_module_id, is_complete, created_at, updated_at
    )
    VALUES (:userId, :courseId, :firstModuleId, false, :now, :now)
    ON CONFLICT (user_id, course_id) DO UPDATE
    SET current_module_id = COALESCE(
          course_progress.current_module_id,
          EXCLUDED.current_module_id
        ),
        updated_at = :now
    """,
        nativeQuery = true
    )
    fun upsert(
        @Param("userId") userId: UUID,
        @Param("courseId") courseId: UUID,
        @Param("firstModuleId") firstModuleId: UUID,
        @Param("now") now: OffsetDateTime
    ): Int

}