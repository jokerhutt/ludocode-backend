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
    WHERE lesson.id IN (:lessonIds)
    ORDER BY lesson.order_index
    """,
        nativeQuery = true
    )
    fun findUserLessonsByIds(
        @Param("lessonIds") lessonIds: List<UUID>,
        @Param("userId") userId: UUID
    ): List<UserLessonProjection>


    @Query(
        "SELECT module_id FROM lesson WHERE id = :lessonId",
        nativeQuery = true
    )
    fun findModuleIdForLesson(lessonId: UUID): UUID?



    @Query(
        """
    WITH ordered AS (
      SELECT l.id AS lesson_id,
             LEAD(l.id) OVER (
               PARTITION BY m.course_id
               ORDER BY m.order_index, l.order_index
             ) AS next_id
      FROM lesson l
      JOIN module m ON m.id = l.module_id
    )
    SELECT next_id
    FROM ordered
    WHERE lesson_id = :currentLesson
    """,
        nativeQuery = true
    )
    fun findNextLessonId(@Param("currentLesson") currentLesson: UUID): UUID?

    @Query(
        """
        select m.course_id
        from lesson l
        join module m on m.id = l.module_id
        where l.id = :lessonId
        """,
        nativeQuery = true
    )
    fun findCourseIdByLesson(@Param("lessonId") lessonId: UUID): UUID?


    @Query(
        value = """
    select l.id
    from lesson l
    join module m on m.id = l.module_id
    where m.course_id = :courseId
    order by m.order_index asc, l.order_index asc
    limit 1
  """,
        nativeQuery = true
    )
    fun findFirstLessonIdInCourse(@Param("courseId") courseId: UUID): UUID?

}