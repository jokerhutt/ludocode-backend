package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

object CourseProgressTestUtil {

    fun pythonProgress(
        userId: UUID,
        courseId: UUID,
        lessonIds: List<UUID>
    ): List<LessonCompletion> =
        lessonIds.map { lessonId ->
            LessonCompletion(
                submissionId = UUID.randomUUID(),
                courseId = courseId,
                userId = userId,
                lessonId = lessonId,
                score = 50,
                accuracy = BigDecimal(50),
                completedAt = OffsetDateTime.now(),
                isDeleted = false
            )
        }

    fun setProgress(
        fromIndex: Int,
        toIndexExclusive: Int,
        userId: UUID,
        courseId: UUID,
        lessonIds: List<UUID>
    ): List<LessonCompletion> {

        val range = lessonIds.subList(fromIndex, toIndexExclusive)

        return range.map { lessonId ->
            LessonCompletion(
                submissionId = UUID.randomUUID(),
                courseId = courseId,
                userId = userId,
                lessonId = lessonId,
                score = 50,
                accuracy = BigDecimal(50),
                completedAt = OffsetDateTime.now(),
                isDeleted = false
            )
        }
    }



}