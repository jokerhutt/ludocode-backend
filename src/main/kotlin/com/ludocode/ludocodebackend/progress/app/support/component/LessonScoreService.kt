package com.ludocode.ludocodebackend.progress.app.support.component

import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeAnswer
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeInteraction
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseAnswer
import com.ludocode.ludocodebackend.lesson.domain.jsonb.SelectAnswer
import com.ludocode.ludocodebackend.lesson.domain.jsonb.SelectInteraction
import com.ludocode.ludocodebackend.lesson.domain.entity.Exercise
import com.ludocode.ludocodebackend.lesson.infra.repository.ExerciseRepository
import com.ludocode.ludocodebackend.progress.api.dto.request.LessonSubmissionRequest
import com.ludocode.ludocodebackend.progress.domain.entity.ExerciseAttempt
import com.ludocode.ludocodebackend.progress.domain.entity.LessonCompletion
import com.ludocode.ludocodebackend.progress.infra.repository.ExerciseAttemptRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

    @Component
    class LessonScoreService(
        private val lessonCompletionRepository: LessonCompletionRepository,
        private val exerciseAttemptRepository: ExerciseAttemptRepository,
        private val exerciseRepository: ExerciseRepository,
        private val clock: Clock
    ) {

        @Transactional
        internal fun addPointsAndCommitSubmission(
            request: LessonSubmissionRequest,
            userId: UUID,
            courseId: UUID
        ): LessonCompletion {

            var scoreForLesson = 0
            var isPerfectLesson = true
            var totalAttempts = 0
            var correctAttempts = 0

            val attemptsToPersist = mutableListOf<ExerciseAttempt>()

            for (submission in request.exercises) {

                val exercise = exerciseRepository
                    .findTopByExerciseId_IdAndIsDeletedFalseOrderByExerciseId_VersionNumberDesc(submission.exerciseId)
                    ?: continue

                val attemptsSize = submission.attempts.size
                totalAttempts += attemptsSize

                val isPerfect = attemptsSize == 1
                if (!isPerfect) isPerfectLesson = false

                for (attempt in submission.attempts) {

                    val isCorrect = grade(exercise, attempt.answer)

                    if (isCorrect) correctAttempts++

                    val score = computeScore(isCorrect, isPerfect)
                    scoreForLesson += score

                    attemptsToPersist.add(
                        ExerciseAttempt(
                            userId = userId,
                            exerciseId = exercise.exerciseId.id,
                            exerciseVersion = exercise.exerciseId.versionNumber,
                            answer = attempt.answer,
                            isCorrect = isCorrect
                        )
                    )
                }
            }

            if (isPerfectLesson) scoreForLesson += 10

            val accuracy = computeAccuracy(correctAttempts, totalAttempts)

            val completion = LessonCompletion(
                submissionId = request.submissionId,
                userId = userId,
                lessonId = request.lessonId,
                courseId = courseId,
                score = scoreForLesson,
                accuracy = accuracy,
                completedAt = OffsetDateTime.now(clock)
            )

            lessonCompletionRepository.save(completion)
            exerciseAttemptRepository.saveAll(attemptsToPersist)

            return completion
        }

        private fun computeScore(isCorrect: Boolean, isPerfect: Boolean): Int =
            when {
                isPerfect && isCorrect -> 5
                isCorrect -> 2
                else -> 0
            }

        private fun computeAccuracy(correct: Int, total: Int): BigDecimal {
            if (total == 0) return BigDecimal.ONE
            return BigDecimal(correct)
                .divide(BigDecimal(total), 2, RoundingMode.HALF_UP)
        }
    }

    //TODO ignore info
    private fun grade(exercise: Exercise, answer: ExerciseAnswer): Boolean {
        val interaction = exercise.interaction ?: return true

        return when (interaction) {

            is SelectInteraction -> {
                val correct = interaction.items
                    .first { it == interaction.correctValue }

                (answer as SelectAnswer).pickedValue == correct
            }

            is ClozeInteraction -> {
                val values = (answer as ClozeAnswer).valuesByBlank

                interaction.blanks.all { blank ->
                    val userValue = values.getOrNull(blank.index) ?: return false
                    userValue in blank.correctOptions
                }
            }
        }
    }

