package com.ludocode.ludocodebackend.progress.app.service

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
    private val lessonCompletionRepository: LessonCompletionRepository
) {

    @Transactional
    fun submitLessonCompletion (request: LessonSubmissionRequest, userId: UUID) : LessonCompletionPacket {

        if (lessonCompletionRepository.existsById(request.id)) {
            return LessonCompletionPacket(content = null, status = LessonCompletionStatus.DUPLICATE)
        }

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

                for (optionId: UUID in attempt.optionIds) {
                    attemptOptions.add(AttemptOption(attemptId = attemptId, optionId = optionId))
                }

            }
            scoreForLesson += scoreForSubmission
        }

        if (isPerfectLesson) scoreForLesson += 10

        val completion = LessonCompletion(userId = userId, score = scoreForLesson, completedAt = OffsetDateTime.now(), lessonId = currentLessonId)

        lessonCompletionRepository.save(completion)
        exerciseAttemptRepository.saveAll(exerciseAttempts)
        attemptOptionRepository.saveAll(attemptOptions)

        val nextLessonId: UUID? = catalogPortForProgress.findNextLessonId(currentLessonId)

        if (nextLessonId == null) {
            return LessonCompletionPacket(content = null, status = LessonCompletionStatus.COURSE_COMPLETE)
        }

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




}