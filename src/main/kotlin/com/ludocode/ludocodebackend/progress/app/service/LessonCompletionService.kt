package com.ludocode.ludocodebackend.progress.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.progress.api.dto.internal.PointsDelta
import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionPacket
import com.ludocode.ludocodebackend.progress.api.dto.response.LessonCompletionResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.StreakResponsePacket
import com.ludocode.ludocodebackend.progress.app.port.out.CatalogPortForProgress
import com.ludocode.ludocodebackend.progress.domain.entity.AttemptOption
import com.ludocode.ludocodebackend.progress.domain.entity.ExerciseAttempt
import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.AttemptOptionId
import com.ludocode.ludocodebackend.progress.domain.enums.LessonCompletionStatus
import com.ludocode.ludocodebackend.progress.infra.repository.AttemptOptionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.ExerciseAttemptRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID


@Service
class LessonCompletionService(
    private val catalogPortForProgress: CatalogPortForProgress,
    private val exerciseAttemptRepository: ExerciseAttemptRepository,
    private val attemptOptionRepository: AttemptOptionRepository,
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

        val lessonCompletion = addPointsAndCommitSubmission(request, userId, courseId)
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

    private fun addPointsAndCommitSubmission (request: LessonSubmissionRequest, userId: UUID, courseId: UUID): LessonCompletion {

        val currentLessonId = request.lessonId

        var scoreForLesson = 0
        var isPerfectLesson = true

        var total = 0
        var correct = 0

        val exerciseAttempts: MutableList<ExerciseAttempt> = mutableListOf()
        val attemptOptions: MutableList<AttemptOption> = mutableListOf()

        for (submission: ExerciseSubmissionRequest in request.submissions) {

            if (submission.attempts[0].answer[0].value == "I") {
                continue
            }

            val version = submission.version

            var scoreForSubmission: Int = 0
            val attemptsSize = submission.attempts.size
            total += attemptsSize
            var isPerfect: Boolean = attemptsSize == 1
            if (!isPerfect) isPerfectLesson = false

            for (attempt: ExerciseAttemptRequest in submission.attempts) {

                if (attempt.isCorrect) correct += 1

                val scoreForAttempt = computeScoreForAttempt(attempt, isPerfect)
                scoreForSubmission += scoreForAttempt
                val attemptId: UUID = UUID.randomUUID()
                val exerciseAttempt = ExerciseAttempt(id = attemptId, userId = userId, exerciseId = attempt.exerciseId, exerciseVersion = version)
                exerciseAttempts.add(exerciseAttempt)

                for (token: AttemptToken in attempt.answer) {
                    attemptOptions.add(AttemptOption(attemptOptionId = AttemptOptionId(attemptId = attemptId, exerciseOptionId = token.id)))
                }

            }
            scoreForLesson += scoreForSubmission
        }

        if (isPerfectLesson) scoreForLesson += 10

        val accuracy = BigDecimal(correct)
            .divide(BigDecimal(total), 2, RoundingMode.HALF_UP)

        val completion = LessonCompletion(id = request.id, userId = userId, score = scoreForLesson, completedAt = OffsetDateTime.now(clock), lessonId = currentLessonId, accuracy = accuracy, courseId = courseId)
        lessonCompletionRepository.save(completion)
        exerciseAttemptRepository.saveAll(exerciseAttempts)
        attemptOptionRepository.saveAll(attemptOptions)

        return completion
    }

    private fun isSubmissionDuplicate (submissionId: UUID): Boolean = lessonCompletionRepository.existsByIdAndIsDeletedFalse(submissionId)

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