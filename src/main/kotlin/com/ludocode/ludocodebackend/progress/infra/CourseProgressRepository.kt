package com.ludocode.ludocodebackend.progress.infra

import com.ludocode.ludocodebackend.progress.domain.entity.CourseProgress
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface CourseProgressRepository : JpaRepository<CourseProgress, CourseProgressId> {

    fun findByIdUserIdAndIdCourseId(userId: UUID, courseId: UUID): CourseProgress?

    fun existsByIdUserIdAndIdCourseId(userId: UUID, courseId: UUID): Boolean

    fun findAllByIdUserId(userId: UUID): List<CourseProgress>

    fun findAllByIdCourseId(courseId: UUID): List<CourseProgress>

    @Modifying
    @Query(
        value = """
        INSERT INTO course_progress (user_id, course_id, current_lesson_id, created_at)
        VALUES (:userId, :courseId, :firstLessonId, now())
        ON CONFLICT (user_id, course_id) DO UPDATE
          SET current_lesson_id = COALESCE(course_progress.current_lesson_id, EXCLUDED.current_lesson_id)
        """,
        nativeQuery = true
    )
    fun upsert(userId: UUID, courseId: UUID, firstLessonId: UUID): Int





}