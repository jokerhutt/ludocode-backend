package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.progress.api.dto.internal.PointsDelta
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.StreakResponsePacket
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForProgress
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.progress.app.support.component.LessonScoreService
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.apache.juli.logging.Log
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID


@Service
class LessonCompletionService(
    private val catalogPortForProgress: CatalogPortForProgress,
    private val lessonScoreService: LessonScoreService,
    private val clock: Clock,
    private val lessonCompletionRepository: LessonCompletionRepository,
    private val userCoinsService: UserCoinsService,
    private val courseProgressService: CourseProgressService,
    private val streakService: StreakService
) {

    private val logger = LoggerFactory.getLogger(LessonCompletionService::class.java)

    @Transactional
    fun submitLessonCompletion (request: LessonSubmissionRequest, userId: UUID) : LessonCompletionPacket {

        val completedLessonId = request.lessonId
        val courseId = request.courseId
        val uniqueSubmissionID = request.submissionId

        if (isSubmissionDuplicate(uniqueSubmissionID)) {
            logger.warn(
                LogEvents.LESSON_COMPLETION_DUPLICATE + " {} {} {}",
                kv(LogFields.SUBMISSION_ID, uniqueSubmissionID.toString()),
                kv(LogFields.LESSON_ID, completedLessonId.toString()),
                kv(LogFields.COURSE_ID, courseId.toString()),
            )
            return LessonCompletionPacket(content = null, status = LessonCompletionStatus.DUPLICATE)
        }

        val lessonCompletion = lessonScoreService.addPointsAndCommitSubmission(request, userId, courseId)
        val scoreForLesson = lessonCompletion.score!!

        val submittedLesson = catalogPortForProgress.findLessonResponseById(completedLessonId, userId)

        if (!submittedLesson.isCompleted) submittedLesson.isCompleted = true

        val newCourseProgressWithCompletion = courseProgressService.updateLesson(userId, courseId = courseId, completedLessonId)

        val newCourseProgress = newCourseProgressWithCompletion!!.courseProgressResponse
        val isFirstCompletion = newCourseProgressWithCompletion!!.isFirstCompletion

        val nowUtc = OffsetDateTime.now(clock)
        val newStreak: StreakResponsePacket = streakService.recordGoalMet(userId, nowUtc)
        val newStats = userCoinsService.apply(PointsDelta(userId = userId, pointsDelta = scoreForLesson))
        val responseContent = LessonCompletionResponse(newStats, newStreak.response, newCourseProgress, submittedLesson, accuracy = lessonCompletion.accuracy)


        if (isFirstCompletion) {
            logger.info(
                LogEvents.LESSON_COMPLETION_SUBMITTED + " {} {} {} {} {} {}",
                kv(LogFields.LESSON_ID, completedLessonId.toString()),
                kv(LogFields.COURSE_ID, courseId.toString()),
                kv(LogFields.SCORE, scoreForLesson),
                kv(LogFields.LESSON_ACCURACY, lessonCompletion.accuracy),
                kv(LogFields.LESSON_STATUS, "COURSE_COMPLETE"),
            )
            return LessonCompletionPacket(content = responseContent, status = LessonCompletionStatus.COURSE_COMPLETE)
        }

        logger.info(
            LogEvents.LESSON_COMPLETION_SUBMITTED + " {} {} {} {} {} {}",
            kv(LogFields.LESSON_ID, completedLessonId.toString()),
            kv(LogFields.COURSE_ID, courseId.toString()),
            kv(LogFields.SCORE, scoreForLesson),
            kv(LogFields.LESSON_ACCURACY, lessonCompletion.accuracy),
            kv(LogFields.LESSON_STATUS, "OK"),
        )

        return LessonCompletionPacket(content = responseContent, status = LessonCompletionStatus.OK)

    }

    private fun isSubmissionDuplicate (submissionId: UUID): Boolean = lessonCompletionRepository.existsBySubmissionIdAndIsDeletedFalse(submissionId)





}