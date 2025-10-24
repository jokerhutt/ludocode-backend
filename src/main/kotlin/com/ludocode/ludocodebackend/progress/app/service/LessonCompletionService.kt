package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.infra.projection.LessonIdTreeProjection
import com.ludocode.ludocodebackend.progress.api.dto.internal.StatsDelta
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPortForProgress
import com.ludocode.ludocodebackend.progress.domain.entity.AttemptOption
import com.ludocode.ludocodebackend.progress.domain.entity.ExerciseAttempt
import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.progress.domain.enums.StreakAction
import com.ludocode.ludocodebackend.progress.infra.repository.AttemptOptionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.ExerciseAttemptRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.UUID


@Service
class LessonCompletionService(
    private val catalogPortForProgress: CatalogPortForProgress,
    private val exerciseAttemptRepository: ExerciseAttemptRepository,
    private val attemptOptionRepository: AttemptOptionRepository,
    private val lessonCompletionRepository: LessonCompletionRepository,
    private val userStatsService: UserStatsService,
    private val courseProgressService: CourseProgressService
) {

    @Transactional
    fun submitLessonCompletion (request: LessonSubmissionRequest, userId: UUID) : LessonCompletionPacket {

        val currentLessonMD : LessonTreeWithIdDTO = catalogPortForProgress.findLessonIdTree(request.lessonId)
            ?: throw IllegalStateException("Lesson not found: ${request.lessonId}")

        val currentLessonId = currentLessonMD.lessonId
        val currentModuleId = currentLessonMD.moduleId
        val nextLessonId = currentLessonMD.nextLessonId
        val courseId = currentLessonMD.courseId

        if (isSubmissionDuplicate(currentLessonId)) return LessonCompletionPacket(content = null, status = LessonCompletionStatus.DUPLICATE)



        val lessonCompletion = addPointsAndCommitSubmission(request, userId)
        val scoreForLesson = lessonCompletion.score!!

        val newStats = userStatsService.apply(StatsDelta(userId = userId, pointsDelta = scoreForLesson, streakAction = StreakAction.NONE))
        val newCourseProgress = courseProgressService.updateLesson(userId, newLessonId = nextLessonId ?: currentLessonId, courseId = courseId)
        val updatedLessonCompletion = catalogPortForProgress.findLessonResponseById(currentLessonId, userId)
        if (!updatedLessonCompletion.isCompleted) updatedLessonCompletion.isCompleted = true
        val responseContent = LessonCompletionResponse(newStats, newCourseProgress, updatedLessonCompletion)

        if (isCourseComplete(nextLessonId)) return LessonCompletionPacket(content = responseContent, status = LessonCompletionStatus.COURSE_COMPLETE)
        return LessonCompletionPacket(content = responseContent, status = LessonCompletionStatus.OK)

    }

    @Transactional
    fun addPointsAndCommitSubmission (request: LessonSubmissionRequest, userId: UUID): LessonCompletion {

        val currentLessonId = request.lessonId

        var scoreForLesson = 0
        var isPerfectLesson = true

        val exerciseAttempts: MutableList<ExerciseAttempt> = mutableListOf()
        val attemptOptions: MutableList<AttemptOption> = mutableListOf()

        for (submission: ExerciseSubmissionRequest in request.submissions) {
            var scoreForSubmission: Int = 0
            var isPerfect: Boolean = submission.attempts.size == 1
            if (!isPerfect) isPerfectLesson = false

            for (attempt: ExerciseAttemptRequest in submission.attempts) {
                scoreForSubmission += computeScoreForAttempt(attempt, isPerfect)
                val attemptId: UUID = UUID.randomUUID()
                val parsedAnswer = attempt.answer.joinToString(" ")
                val exerciseAttempt = ExerciseAttempt(id = attemptId, content = parsedAnswer, exerciseId = attempt.exerciseId)
                exerciseAttempts.add(exerciseAttempt)

                for (token: String in attempt.answer) {
                    attemptOptions.add(AttemptOption(attemptId = attemptId, token = token))
                }

            }
            scoreForLesson += scoreForSubmission
        }

        if (isPerfectLesson) scoreForLesson += 10

        val completion = LessonCompletion(id = request.id, userId = userId, score = scoreForLesson, completedAt = OffsetDateTime.now(), lessonId = currentLessonId)
        lessonCompletionRepository.save(completion)
        exerciseAttemptRepository.saveAll(exerciseAttempts)
        attemptOptionRepository.saveAll(attemptOptions)

        return completion
    }

    private fun isCourseComplete (nextLessonId: UUID?): Boolean = nextLessonId == null
    private fun isSubmissionDuplicate (submissionId: UUID): Boolean = lessonCompletionRepository.existsById(submissionId)

    private fun computeScoreForAttempt (attempt: ExerciseAttemptRequest, isPerfect: Boolean) : Int {
        if (isPerfect) {
            return 5
        } else if (attempt.isCorrect) {
            return 2
        } else {
            return 0
        }
    }




}