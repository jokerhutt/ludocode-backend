package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.infra.projection.UserLessonProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LessonRepository : JpaRepository<Lesson, UUID> {

    @Query(
        value = """
        SELECT lesson.id AS id, lesson.title AS title, lesson.order_index AS orderIndex,
            EXISTS (
                SELECT 1
                FROM lesson_completion lessonCompletion
                WHERE lessonCompletion.lesson_id = lesson.id 
                AND lessonCompletion.user_id = :userId
            ) AS isCompleted
        FROM lesson lesson
        WHERE lesson.module_id = :moduleId
        ORDER BY lesson.order_index
    """,
        nativeQuery = true
    )
    fun findUserLessons(
        @Param("moduleId") moduleId: UUID,
        @Param("userId") userId: UUID
    ): List<UserLessonProjection>


}