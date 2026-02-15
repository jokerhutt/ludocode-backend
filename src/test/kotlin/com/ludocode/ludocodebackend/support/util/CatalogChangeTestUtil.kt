package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.support.snapshot.CourseSnap
import java.util.UUID
import kotlin.random.Random

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

    /**
     * Generates random curriculum changes for testing
     * @param courseSnapshot The original course snapshot
     * @param seed Random seed for reproducible tests (optional)
     * @return Modified curriculum draft with random changes
     */
    fun generateRandomCurriculumChanges(
        courseSnapshot: CourseSnap,
        seed: Long? = null
    ): CurriculumDraftSnapshot {
        val random = seed?.let { Random(it) } ?: Random.Default
        val curriculum = toCurriculumDraft(courseSnapshot)

        // Define possible edit events
        val events = listOf(
            "CHANGE_LESSON_TITLE",
            "ADD_LESSONS_TO_MODULE",
            "ADD_NEW_MODULE",
            "DELETE_FIRST_LESSON",
            "DELETE_LAST_LESSON",
            "DELETE_MIDDLE_LESSON",
            "DELETE_RANDOM_LESSON",
            "DELETE_ENTIRE_MODULE",
            "REORDER_LESSONS",
            "RENAME_MODULE"
        )

        // Execute 1-5 random events
        val numEvents = random.nextInt(1, 6)
        repeat(numEvents) {
            val event = events[random.nextInt(events.size)]

            when (event) {
                "CHANGE_LESSON_TITLE" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val module = curriculum.modules[moduleIndex]
                        if (module.lessons.isNotEmpty()) {
                            val lessonIndex = random.nextInt(module.lessons.size)
                            module.lessons[lessonIndex].title = "Modified ${randomString(random, 10)}"
                        }
                    }
                }

                "ADD_LESSONS_TO_MODULE" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val numLessons = random.nextInt(1, 4)
                        repeat(numLessons) {
                            curriculum.modules[moduleIndex].lessons += createLesson("Random Lesson ${randomString(random, 5)}")
                        }
                    }
                }

                "ADD_NEW_MODULE" -> {
                    val numLessons = random.nextInt(1, 4)
                    val lessonTitles = (1..numLessons).map { "Lesson ${randomString(random, 5)}" }.toTypedArray()
                    curriculum.modules += createModule("Random Module ${randomString(random, 5)}", *lessonTitles)
                }

                "DELETE_FIRST_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val module = curriculum.modules[moduleIndex]
                        if (module.lessons.size > 1) {
                            module.lessons = module.lessons.drop(1)
                        }
                    }
                }

                "DELETE_LAST_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val module = curriculum.modules[moduleIndex]
                        if (module.lessons.size > 1) {
                            module.lessons = module.lessons.dropLast(1)
                        }
                    }
                }

                "DELETE_MIDDLE_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val module = curriculum.modules[moduleIndex]
                        if (module.lessons.size > 2) {
                            val middleIndex = module.lessons.size / 2
                            module.lessons = module.lessons.filterIndexed { index, _ -> index != middleIndex }
                        }
                    }
                }

                "DELETE_RANDOM_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val module = curriculum.modules[moduleIndex]
                        if (module.lessons.size > 1) {
                            val indexToDelete = random.nextInt(module.lessons.size)
                            module.lessons = module.lessons.filterIndexed { index, _ -> index != indexToDelete }
                        }
                    }
                }

                "DELETE_ENTIRE_MODULE" -> {
                    if (curriculum.modules.size > 1) {
                        val indexToDelete = random.nextInt(curriculum.modules.size)
                        curriculum.modules = curriculum.modules.filterIndexed { index, _ -> index != indexToDelete }
                    }
                }

                "REORDER_LESSONS" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val module = curriculum.modules[moduleIndex]
                        if (module.lessons.size > 1) {
                            module.lessons = module.lessons.shuffled(random)
                        }
                    }
                }

                "RENAME_MODULE" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val oldModule = curriculum.modules[moduleIndex]
                        // Create new module with renamed title (title is immutable)
                        curriculum.modules = curriculum.modules.mapIndexed { index, module ->
                            if (index == moduleIndex) {
                                ModuleDraftSnapshot(
                                    id = oldModule.id,
                                    title = "Renamed ${randomString(random, 10)}",
                                    lessons = oldModule.lessons
                                )
                            } else {
                                module
                            }
                        }
                    }
                }
            }
        }

        return curriculum
    }

    /**
     * Generates random exercise changes for a lesson
     * @param exercises The original exercises
     * @param seed Random seed for reproducible tests (optional)
     * @return Modified list of exercises with random changes
     */
    fun generateRandomExerciseChanges(
        exercises: List<ExerciseSnap>,
        seed: Long? = null
    ): MutableList<ExerciseSnap> {
        val random = seed?.let { Random(it) } ?: Random.Default
        val exerciseList = exercises.toMutableList()

        // Define possible edit events
        val events = listOf(
            "CHANGE_TITLE",
            "UPDATE_OPTIONS",
            "ADD_EXERCISES",
            "DELETE_FIRST_EXERCISE",
            "DELETE_LAST_EXERCISE",
            "DELETE_MIDDLE_EXERCISE",
            "DELETE_RANDOM_EXERCISE",
            "REORDER_EXERCISES",
            "CHANGE_EXERCISE_TYPE"
        )

        // Execute 1-4 random events
        val numEvents = random.nextInt(1, 5)
        repeat(numEvents) {
            val event = events[random.nextInt(events.size)]

            when (event) {
                "CHANGE_TITLE" -> {
                    if (exerciseList.isNotEmpty()) {
                        val exerciseIndex = random.nextInt(exerciseList.size)
                        exerciseList[exerciseIndex].title = "Modified ${randomString(random, 10)}"
                    }
                }

                "UPDATE_OPTIONS" -> {
                    if (exerciseList.isNotEmpty()) {
                        val exerciseIndex = random.nextInt(exerciseList.size)
                        val exercise = exerciseList[exerciseIndex]
                        if (exercise.exerciseType != ExerciseType.INFO) {
                            val numCorrect = when (exercise.exerciseType) {
                                ExerciseType.TRIVIA, ExerciseType.ANALYZE -> 1
                                ExerciseType.CLOZE -> random.nextInt(1, 4)
                                else -> 1
                            }
                            val numDistractors = random.nextInt(1, 4)
                            updateExerciseOptions(
                                exercise,
                                correctAnswers = (1..numCorrect).map { randomString(random, 8) },
                                distractors = (1..numDistractors).map { randomString(random, 8) }
                            )
                        }
                    }
                }

                "ADD_EXERCISES" -> {
                    val numToAdd = random.nextInt(1, 3)
                    repeat(numToAdd) {
                        val exerciseType = ExerciseType.entries[random.nextInt(ExerciseType.entries.size)]
                        exerciseList += when (exerciseType) {
                            ExerciseType.INFO -> createInfoExercise("Info ${randomString(random, 8)}")
                            ExerciseType.TRIVIA -> createTriviaExercise(
                                title = "Trivia ${randomString(random, 8)}",
                                correctAnswer = randomString(random, 10),
                                distractors = arrayOf(randomString(random, 10), randomString(random, 10))
                            )
                            ExerciseType.CLOZE -> createClozeExercise(
                                title = "Cloze ${randomString(random, 8)}",
                                prompt = "Fill ${randomString(random, 5)} the blanks",
                                correctAnswers = listOf(randomString(random, 5), randomString(random, 5)),
                                distractors = listOf(randomString(random, 5))
                            )
                            ExerciseType.ANALYZE -> createAnalyzeExercise(
                                title = "Analyze ${randomString(random, 8)}",
                                prompt = "What does this ${randomString(random, 10)} do?",
                                correctAnswer = randomString(random, 10),
                                distractors = arrayOf(randomString(random, 10), randomString(random, 10))
                            )
                        }
                    }
                }

                "DELETE_FIRST_EXERCISE" -> {
                    if (exerciseList.size > 1) {
                        exerciseList.removeAt(0)
                    }
                }

                "DELETE_LAST_EXERCISE" -> {
                    if (exerciseList.size > 1) {
                        exerciseList.removeAt(exerciseList.size - 1)
                    }
                }

                "DELETE_MIDDLE_EXERCISE" -> {
                    if (exerciseList.size > 2) {
                        val middleIndex = exerciseList.size / 2
                        exerciseList.removeAt(middleIndex)
                    }
                }

                "DELETE_RANDOM_EXERCISE" -> {
                    if (exerciseList.size > 1) {
                        val indexToDelete = random.nextInt(exerciseList.size)
                        exerciseList.removeAt(indexToDelete)
                    }
                }

                "REORDER_EXERCISES" -> {
                    if (exerciseList.size > 1) {
                        val shuffled = exerciseList.toMutableList().apply { shuffle(random) }
                        exerciseList.clear()
                        exerciseList.addAll(shuffled)
                    }
                }

                "CHANGE_EXERCISE_TYPE" -> {
                    if (exerciseList.isNotEmpty()) {
                        val exerciseIndex = random.nextInt(exerciseList.size)
                        val oldExercise = exerciseList[exerciseIndex]
                        val newType = ExerciseType.entries[random.nextInt(ExerciseType.entries.size)]

                        // Replace with new exercise of different type
                        exerciseList[exerciseIndex] = when (newType) {
                            ExerciseType.INFO -> createInfoExercise(oldExercise.title)
                            ExerciseType.TRIVIA -> createTriviaExercise(
                                title = oldExercise.title,
                                correctAnswer = randomString(random, 10),
                                distractors = arrayOf(randomString(random, 10), randomString(random, 10))
                            )
                            ExerciseType.CLOZE -> createClozeExercise(
                                title = oldExercise.title,
                                prompt = "Fill the blanks",
                                correctAnswers = listOf(randomString(random, 8)),
                                distractors = listOf(randomString(random, 8))
                            )
                            ExerciseType.ANALYZE -> createAnalyzeExercise(
                                title = oldExercise.title,
                                prompt = "What does this do?",
                                correctAnswer = randomString(random, 10),
                                distractors = arrayOf(randomString(random, 10))
                            )
                        }
                    }
                }
            }
        }

        return exerciseList
    }

    /**
     * Generates a random alphanumeric string
     */
    private fun randomString(random: Random, length: Int): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { chars[random.nextInt(chars.size)] }
            .joinToString("")
    }
}
