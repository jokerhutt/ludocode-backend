package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.progress.api.dto.internal.PointsDelta
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.StreakResponsePacket
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPortForProgress
import com.ludocode.ludocodebackend.progress.app.support.component.LessonScoreService
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
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

    @Transactional
    fun submitLessonCompletion (request: LessonSubmissionRequest, userId: UUID) : LessonCompletionPacket {

        val currentLessonMD : LessonTreeWithIdDTO = catalogPortForProgress.findLessonIdTree(request.lessonId)

        val currentLessonId = currentLessonMD.lessonId
        val nextLessonId = currentLessonMD.nextLessonId
        val courseId = currentLessonMD.courseId

        if (isSubmissionDuplicate(currentLessonId)) return LessonCompletionPacket(content = null, status = LessonCompletionStatus.DUPLICATE)

        val lessonCompletion = lessonScoreService.addPointsAndCommitSubmission(request, userId, courseId)
        val scoreForLesson = lessonCompletion.score!!

        val submittedLesson = catalogPortForProgress.findLessonResponseById(currentLessonId, userId)
        val isCompleted = submittedLesson.isCompleted

        println("IsCompleted = $isCompleted")
        if (!submittedLesson.isCompleted) submittedLesson.isCompleted = true


        val newCourseProgressWithCompletion = courseProgressService.updateLesson(userId, currentLessonId = currentLessonId, newLessonId = nextLessonId, isCompleted = isCompleted, courseId = courseId)

        val newCourseProgress = newCourseProgressWithCompletion!!.courseProgressResponse
        val isFirstCompletion = newCourseProgressWithCompletion!!.isFirstCompletion

        val nowUtc = OffsetDateTime.now(clock)
        val newStreak: StreakResponsePacket = streakService.recordGoalMet(userId, nowUtc)
        val newStats = userCoinsService.apply(PointsDelta(userId = userId, pointsDelta = scoreForLesson))
        val responseContent = LessonCompletionResponse(newStats, newStreak.response, newCourseProgress, submittedLesson, accuracy = lessonCompletion.accuracy)

        if (isFirstCompletion) return LessonCompletionPacket(content = responseContent, status = LessonCompletionStatus.COURSE_COMPLETE)

        return LessonCompletionPacket(content = responseContent, status = LessonCompletionStatus.OK)

    }

    private fun isSubmissionDuplicate (submissionId: UUID): Boolean = lessonCompletionRepository.existsByIdAndIsDeletedFalse(submissionId)





}