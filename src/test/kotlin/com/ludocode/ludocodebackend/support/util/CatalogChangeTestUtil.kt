package com.ludocode.ludocodebackend.support.util
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.support.snapshot.CourseSnap
import java.util.UUID

object CatalogChangeTestUtil {

    fun toCurriculumDraft(courseSnapshot: CourseSnap): CurriculumDraftSnapshot {
        return CurriculumDraftSnapshot(
            modules = courseSnapshot.modules.map { module ->
                ModuleDraftSnapshot(
                    id = module.moduleId,
                    title = module.title,
                    lessons = module.lessons.map { lesson ->
                        LessonDraftSnapshot(
                            id = lesson.id,
                            title = lesson.title
                        )
                    }
                )
            }
        )
    }

    fun createLesson(title: String): LessonDraftSnapshot {
        return LessonDraftSnapshot(
            id = UUID.randomUUID(),
            title = title
        )
    }

    fun createModule(title: String, vararg lessonTitles: String): ModuleDraftSnapshot {
        return ModuleDraftSnapshot(
            id = UUID.randomUUID(),
            title = title,
            lessons = lessonTitles.map { createLesson(it) }
        )
    }

    fun createInfoExercise(title: String): ExerciseSnap {
        return ExerciseSnap(
            id = UUID.randomUUID(),
            title = title,
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.INFO,
            correctOptions = listOf(),
            distractors = listOf()
        )
    }

    fun createTriviaExercise(
        title: String,
        subtitle: String? = null,
        correctAnswer: String,
        vararg distractors: String
    ): ExerciseSnap {
        return ExerciseSnap(
            id = UUID.randomUUID(),
            title = title,
            subtitle = subtitle,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.TRIVIA,
            correctOptions = listOf(
                OptionSnap(
                    content = correctAnswer,
                    answerOrder = 1,
                    exerciseOptionId = UUID.randomUUID()
                )
            ),
            distractors = distractors.mapIndexed { _, distractor ->
                OptionSnap(
                    content = distractor,
                    answerOrder = null,
                    exerciseOptionId = UUID.randomUUID()
                )
            }
        )
    }

    fun createClozeExercise(
        title: String,
        subtitle: String? = null,
        prompt: String,
        correctAnswers: List<String>,
        distractors: List<String> = listOf(),
        media: String? = null
    ): ExerciseSnap {
        return ExerciseSnap(
            id = UUID.randomUUID(),
            title = title,
            subtitle = subtitle,
            prompt = prompt,
            media = media,
            exerciseType = ExerciseType.CLOZE,
            correctOptions = correctAnswers.mapIndexed { index, answer ->
                OptionSnap(
                    content = answer,
                    answerOrder = index + 1,
                    exerciseOptionId = UUID.randomUUID()
                )
            },
            distractors = distractors.map { distractor ->
                OptionSnap(
                    content = distractor,
                    answerOrder = null,
                    exerciseOptionId = UUID.randomUUID()
                )
            }
        )
    }


    fun createAnalyzeExercise(
        title: String,
        subtitle: String? = null,
        prompt: String,
        correctAnswer: String,
        vararg distractors: String,
        media: String? = null
    ): ExerciseSnap {
        return ExerciseSnap(
            id = UUID.randomUUID(),
            title = title,
            subtitle = subtitle,
            prompt = prompt,
            media = media,
            exerciseType = ExerciseType.ANALYZE,
            correctOptions = listOf(
                OptionSnap(
                    content = correctAnswer,
                    answerOrder = 1,
                    exerciseOptionId = UUID.randomUUID()
                )
            ),
            distractors = distractors.map { distractor ->
                OptionSnap(
                    content = distractor,
                    answerOrder = null,
                    exerciseOptionId = UUID.randomUUID()
                )
            }
        )
    }

    fun updateExerciseOptions(
        exercise: ExerciseSnap,
        correctAnswers: List<String>,
        distractors: List<String> = listOf()
    ) {
        exercise.correctOptions = correctAnswers.mapIndexed { index, answer ->
            OptionSnap(
                content = answer,
                answerOrder = if (exercise.exerciseType == ExerciseType.TRIVIA || exercise.exerciseType == ExerciseType.ANALYZE) 1 else index + 1,
                exerciseOptionId = UUID.randomUUID()
            )
        }
        exercise.distractors = distractors.map { distractor ->
            OptionSnap(
                content = distractor,
                answerOrder = null,
                exerciseOptionId = UUID.randomUUID()
            )
        }
    }
}
