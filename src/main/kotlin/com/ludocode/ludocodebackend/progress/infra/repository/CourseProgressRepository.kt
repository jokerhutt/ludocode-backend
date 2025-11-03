package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.infra.projection.CourseProgressWithModuleProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface CourseProgressRepository : JpaRepository<CourseProgress, CourseProgressId> {

    @Query(
        value = """
        select cp.course_id, cp.user_id, cp.current_lesson_id, l.module_id
        from course_progress cp
        left join exercise l on l.id = cp.current_lesson_id
        where cp.user_id = :userId and cp.course_id = :courseId
        """,
        nativeQuery = true
    )
    fun findProgressWithModule(userId: UUID, courseId: UUID): CourseProgressWithModuleProjection

    @Modifying
    @Query(""" 
        update course_progress cp
        set current_lesson_id = :newLessonId
        where user_id = :userId
        and course_id = :courseId
    """, nativeQuery = true)
    fun resetCourseProgressForUser(userId: UUID, courseId: UUID, newLessonId: UUID)


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
    select 
      cp.course_id         as courseId,
      cp.user_id           as userId,
      cp.current_lesson_id as currentLessonId,
      l.module_id          as moduleId
    from course_progress cp
    left join exercise l on l.id = cp.current_lesson_id
    where cp.user_id = :userId
      and cp.course_id in (:courseIds)
  """,
        nativeQuery = true
    )
    fun findAllProgressWithModulesByUserAndCourses(
        userId: UUID,
        courseIds: List<UUID>
    ): List<CourseProgressWithModuleProjection>

    @Modifying
    @Query(
        """
    update course_progress
       set current_lesson_id = :newLessonId
       where user_id = :userId
       and course_id = :courseId
    """,
        nativeQuery = true
    )
    fun setCurrentLesson(userId: UUID, courseId: UUID, newLessonId: UUID): Int



    @Modifying
    @Query(
        """
  INSERT INTO course_progress (user_id, course_id, current_lesson_id, is_complete, created_at)
  VALUES (:userId, :courseId, :firstLessonId, false, now())
  ON CONFLICT (user_id, course_id) DO UPDATE
    SET current_lesson_id = COALESCE(course_progress.current_lesson_id, EXCLUDED.current_lesson_id)
  """,
        nativeQuery = true
    )
    fun upsert(userId: UUID, courseId: UUID, firstLessonId: UUID): Int

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