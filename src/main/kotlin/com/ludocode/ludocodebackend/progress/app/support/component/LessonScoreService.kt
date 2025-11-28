package com.ludocode.ludocodebackend.progress.app.support.component

import com.ludocode.ludocodebackend.progress.api.dto.request.AttemptToken
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseAttemptRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.ExerciseSubmissionRequest
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.domain.entity.AttemptOption
import com.ludocode.ludocodebackend.progress.domain.entity.ExerciseAttempt
import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.AttemptOptionId
import com.ludocode.ludocodebackend.progress.infra.repository.AttemptOptionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.ExerciseAttemptRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Component
class LessonScoreService(
    private val lessonCompletionRepository: LessonCompletionRepository,
    private val exerciseAttemptRepository: ExerciseAttemptRepository,
    private val clock: Clock,
    private val attemptOptionRepository: AttemptOptionRepository
) {

    @Transactional
    internal fun addPointsAndCommitSubmission (request: LessonSubmissionRequest, userId: UUID, courseId: UUID): LessonCompletion {

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
                val exerciseAttempt = ExerciseAttempt(
                    id = attemptId,
                    userId = userId,
                    exerciseId = attempt.exerciseId,
                    exerciseVersion = version
                )
                exerciseAttempts.add(exerciseAttempt)

                for (token: AttemptToken in attempt.answer) {
                    attemptOptions.add(
                        AttemptOption(
                            attemptOptionId = AttemptOptionId(
                                attemptId = attemptId,
                                exerciseOptionId = token.id
                            )
                        )
                    )
                }

            }
            scoreForLesson += scoreForSubmission
        }

        if (isPerfectLesson) scoreForLesson += 10

        val accuracy = computeAccuracy(correct, total)

        val completion = LessonCompletion(
            id = request.id,
            userId = userId,
            score = scoreForLesson,
            completedAt = OffsetDateTime.now(clock),
            lessonId = currentLessonId,
            accuracy = accuracy,
            courseId = courseId
        )
        lessonCompletionRepository.save(completion)
        exerciseAttemptRepository.saveAll(exerciseAttempts)
        attemptOptionRepository.saveAll(attemptOptions)

        return completion
    }

    private fun computeScoreForAttempt (attempt: ExerciseAttemptRequest, isPerfect: Boolean) : Int {
        if (isPerfect) {
            return 5
        } else if (attempt.isCorrect) {
            return 2
        } else {
            return 0
        }
    }

    private fun computeAccuracy (correct: Int, total: Int): BigDecimal {
        if (total == 0) return BigDecimal(1); //Entire exercise is INFO
        return BigDecimal(correct)
            .divide(BigDecimal(total), 2, RoundingMode.HALF_UP)

    }



}