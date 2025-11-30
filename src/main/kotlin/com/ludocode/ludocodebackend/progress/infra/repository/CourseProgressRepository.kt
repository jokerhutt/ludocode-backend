package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.infra.projection.CourseProgressWithModuleProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface CourseProgressRepository : JpaRepository<CourseProgress, CourseProgressId> {

    @Query(value = """
  SELECT 
    cp.course_id         AS courseId,
    cp.user_id           AS userId,
    cp.current_lesson_id AS currentLessonId,
    cp.updated_at        AS updatedAt,
    ml.module_id         AS moduleId
  FROM course_progress cp
  JOIN module_lessons ml
    ON ml.lesson_id = cp.current_lesson_id
  JOIN module m
    ON m.id = ml.module_id
   AND m.course_id = cp.course_id
  WHERE cp.user_id = :userId
    AND cp.course_id = :courseId
""", nativeQuery = true)
    fun findProgressWithModule(
        @Param("userId") userId: UUID,
        @Param("courseId") courseId: UUID
    ): CourseProgressWithModuleProjection

    @Modifying
    @Query("""
    UPDATE course_progress
    SET current_lesson_id = :newLessonId,
        is_complete = false
    WHERE user_id = :userId
      AND course_id = :courseId
""", nativeQuery = true)
    fun resetCourseProgressForUser(
        @Param("userId") userId: UUID,
        @Param("courseId") courseId: UUID,
        @Param("newLessonId") newLessonId: UUID
    )


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

    @Query(value = """
  SELECT 
    cp.course_id         AS courseId,
    cp.user_id           AS userId,
    cp.current_lesson_id AS currentLessonId,
    ml.module_id         AS moduleId,
    cp.updated_at        AS updatedAt
  FROM course_progress cp
  JOIN module_lessons ml
    ON ml.lesson_id = cp.current_lesson_id
  JOIN module m
    ON m.id = ml.module_id
   AND m.course_id = cp.course_id
  WHERE cp.user_id = :userId
    AND cp.course_id IN (:courseIds)
""", nativeQuery = true)
    fun findAllProgressWithModulesByUserAndCourses(
        @Param("userId") userId: UUID,
        @Param("courseIds") courseIds: List<UUID>
    ): List<CourseProgressWithModuleProjection>

    @Modifying
    @Query(
        """
  INSERT INTO course_progress (
    user_id, course_id, current_lesson_id, is_complete, created_at, updated_at
  ) VALUES (:userId, :courseId, :firstLessonId, false, :now, :now)
  ON CONFLICT (user_id, course_id) DO UPDATE
  SET current_lesson_id = COALESCE(course_progress.current_lesson_id, EXCLUDED.current_lesson_id),
      updated_at        = :now
  """,
        nativeQuery = true
    )
    fun upsert(userId: UUID, courseId: UUID, firstLessonId: UUID, @Param("now") now: OffsetDateTime): Int

    //CAN STAY
    @Modifying
    @Query(
        """
    update course_progress
       set is_complete = true
       where user_id = :userId
       and course_id = :courseId
    """, nativeQuery = true
    )
    fun markCourseComplete(userId: UUID, courseId: UUID): Int





}