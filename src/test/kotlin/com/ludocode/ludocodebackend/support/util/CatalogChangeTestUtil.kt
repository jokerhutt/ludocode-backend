package com.ludocode.ludocodebackend.support.util

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.lesson.domain.jsonb.Block
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeInteraction
import com.ludocode.ludocodebackend.lesson.domain.jsonb.CodeBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ExerciseInteraction
import com.ludocode.ludocodebackend.lesson.domain.jsonb.HeaderBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionBlank
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionFile
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ParagraphBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.SelectInteraction
import com.ludocode.ludocodebackend.support.snapshot.CourseSnap

import java.util.UUID
import kotlin.random.Random

object CatalogChangeTestUtil {

    fun toCurriculumDraft(courseSnapshot: CourseSnap): CurriculumDraftSnapshot =
        CurriculumDraftSnapshot(
            modules = courseSnapshot.modules.map { module ->
                ModuleDraftSnapshot(
                    id = module.moduleId,
                    title = module.title,

                    lessons = module.lessons.map { lesson ->
                        LessonDraftSnapshot(
                            id = lesson.id,
                            title = lesson.title,
                            lessonType = LessonType.NORMAL
                        )
                    }
                )
            }
        )

    fun createLesson(title: String): LessonDraftSnapshot =
        LessonDraftSnapshot(
            id = UUID.randomUUID(),
            lessonType = LessonType.NORMAL,
            title = title
        )

    fun createModule(title: String, vararg lessonTitles: String): ModuleDraftSnapshot =
        ModuleDraftSnapshot(
            id = UUID.randomUUID(),
            title = title,
            lessons = lessonTitles.map { createLesson(it) }
        )


    fun createInfoExercise(text: String): ExerciseSnap =
        ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(ParagraphBlock(text)),
            interaction = null
        )

    fun createSelectExercise(
        title: String,
        subtitle: String? = null,
        code: CodeBlock? = null,
        correctValue: String,
        vararg distractors: String
    ): ExerciseSnap {
        val blocks = buildList<Block> {
            add(HeaderBlock(title))
            if (!subtitle.isNullOrBlank()) add(ParagraphBlock(subtitle))
            if (code != null) add(code)
        }

        val items = (listOf(correctValue) + distractors.toList()).shuffled()

        return ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = blocks,
            interaction = SelectInteraction(
                items = items,
                correctValue = correctValue
            )
        )
    }

    fun createClozeExercise(
        title: String,
        subtitle: String? = null,
        language: String,
        content: String,
        // each blank can have multiple allowed values
        correctValuesByBlank: List<List<String>>,
        options: List<String>,
        codeBlockInBlocks: CodeBlock? = null
    ): ExerciseSnap {
        val blocks = buildList<Block> {
            add(HeaderBlock(title))
            if (!subtitle.isNullOrBlank()) add(ParagraphBlock(subtitle))
            if (codeBlockInBlocks != null) add(codeBlockInBlocks)
        }

        return ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = blocks,
            interaction = ClozeInteraction(
                file = InteractionFile(language = language, content = content),
                blanks = correctValuesByBlank.mapIndexed { idx, allowed ->
                    InteractionBlank(index = idx, correctOptions = allowed)
                },
                options = options
            )
        )
    }

    // “update options” now means: replace the interaction
    fun updateExerciseInteraction(exercise: ExerciseSnap, newInteraction: ExerciseInteraction?) {
        exercise.interaction = newInteraction
    }

    // -------------------------
    // Random Curriculum Changes
    // (unchanged; exercises handled elsewhere)
    // -------------------------

    fun generateRandomCurriculumChanges(
        courseSnapshot: CourseSnap,
        seed: Long? = null
    ): CurriculumDraftSnapshot {
        val random = seed?.let { Random(it) } ?: Random.Default
        val curriculum = toCurriculumDraft(courseSnapshot)

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

        val numEvents = random.nextInt(1, 6)
        repeat(numEvents) {
            when (events[random.nextInt(events.size)]) {

                "CHANGE_LESSON_TITLE" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val module = curriculum.modules[random.nextInt(curriculum.modules.size)]
                        if (module.lessons.isNotEmpty()) {
                            val i = random.nextInt(module.lessons.size)
                            module.lessons[i].title = "Modified ${randomString(random, 10)}"
                        }
                    }
                }

                "ADD_LESSONS_TO_MODULE" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val numLessons = random.nextInt(1, 4)
                        repeat(numLessons) {
                            curriculum.modules[moduleIndex].lessons += createLesson(
                                "Random Lesson ${randomString(random, 5)}"
                            )
                        }
                    }
                }

                "ADD_NEW_MODULE" -> {
                    val numLessons = random.nextInt(1, 4)
                    val lessonTitles = (1..numLessons)
                        .map { "Lesson ${randomString(random, 5)}" }
                        .toTypedArray()
                    curriculum.modules += createModule("Random Module ${randomString(random, 5)}", *lessonTitles)
                }

                "DELETE_FIRST_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val module = curriculum.modules[random.nextInt(curriculum.modules.size)]
                        if (module.lessons.size > 1) module.lessons = module.lessons.drop(1)
                    }
                }

                "DELETE_LAST_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val module = curriculum.modules[random.nextInt(curriculum.modules.size)]
                        if (module.lessons.size > 1) module.lessons = module.lessons.dropLast(1)
                    }
                }

                "DELETE_MIDDLE_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val module = curriculum.modules[random.nextInt(curriculum.modules.size)]
                        if (module.lessons.size > 2) {
                            val middle = module.lessons.size / 2
                            module.lessons = module.lessons.filterIndexed { idx, _ -> idx != middle }
                        }
                    }
                }

                "DELETE_RANDOM_LESSON" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val module = curriculum.modules[random.nextInt(curriculum.modules.size)]
                        if (module.lessons.size > 1) {
                            val del = random.nextInt(module.lessons.size)
                            module.lessons = module.lessons.filterIndexed { idx, _ -> idx != del }
                        }
                    }
                }

                "DELETE_ENTIRE_MODULE" -> {
                    if (curriculum.modules.size > 1) {
                        val del = random.nextInt(curriculum.modules.size)
                        curriculum.modules = curriculum.modules.filterIndexed { idx, _ -> idx != del }
                    }
                }

                "REORDER_LESSONS" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val module = curriculum.modules[random.nextInt(curriculum.modules.size)]
                        if (module.lessons.size > 1) module.lessons = module.lessons.shuffled(random)
                    }
                }

                "RENAME_MODULE" -> {
                    if (curriculum.modules.isNotEmpty()) {
                        val moduleIndex = random.nextInt(curriculum.modules.size)
                        val old = curriculum.modules[moduleIndex]
                        curriculum.modules = curriculum.modules.mapIndexed { idx, m ->
                            if (idx == moduleIndex)
                                ModuleDraftSnapshot(old.id, "Renamed ${randomString(random, 10)}", old.lessons)
                            else m
                        }
                    }
                }
            }
        }

        return curriculum
    }

    // -------------------------
    // Random Exercise Changes
    // Now edits blocks + interaction only
    // -------------------------

    fun generateRandomExerciseChanges(
        exercises: List<ExerciseSnap>,
        seed: Long? = null
    ): MutableList<ExerciseSnap> {
        val random = seed?.let { Random(it) } ?: Random.Default
        val list = exercises.toMutableList()

        val events = listOf(
            "CHANGE_TITLE_BLOCK",
            "UPDATE_INTERACTION",
            "ADD_EXERCISES",
            "DELETE_FIRST",
            "DELETE_LAST",
            "DELETE_MIDDLE",
            "DELETE_RANDOM",
            "REORDER",
            "CHANGE_KIND"
        )

        val numEvents = random.nextInt(1, 5)
        repeat(numEvents) {
            when (events[random.nextInt(events.size)]) {

                "CHANGE_TITLE_BLOCK" -> {
                    if (list.isNotEmpty()) {
                        val i = random.nextInt(list.size)
                        list[i] = list[i].copy(
                            blocks = list[i].blocks.map {
                                if (it is HeaderBlock) HeaderBlock("Modified ${randomString(random, 10)}") else it
                            }
                        )
                    }
                }

                "UPDATE_INTERACTION" -> {
                    if (list.isNotEmpty()) {
                        val i = random.nextInt(list.size)
                        val ex = list[i]

                        val interaction = ex.interaction

                        val newInteraction: ExerciseInteraction? = when (interaction) {
                            is SelectInteraction -> {
                                val correct = randomString(random, 6)
                                val d1 = randomString(random, 6)
                                val d2 = randomString(random, 6)
                                SelectInteraction(
                                    items = listOf(correct, d1, d2).shuffled(random),
                                    correctValue = correct
                                )
                            }

                            is ClozeInteraction -> {
                                val correct0 = randomString(random, 5)
                                ClozeInteraction(
                                    file = interaction.file,
                                    blanks = listOf(InteractionBlank(0, listOf(correct0))),
                                    options = listOf(correct0, randomString(random, 5))
                                )
                            }

                            null -> null
                        }

                        list[i] = ex.copy(interaction = newInteraction)
                    }
                }

                "ADD_EXERCISES" -> {
                    val numToAdd = random.nextInt(1, 3)
                    repeat(numToAdd) {
                        val kind = random.nextInt(3)
                        list += when (kind) {
                            0 -> createInfoExercise("Info ${randomString(random, 8)}")
                            1 -> createSelectExercise(
                                title = "Select ${randomString(random, 8)}",
                                correctValue = randomString(random, 6),
                                distractors = arrayOf(randomString(random, 6), randomString(random, 6))
                            )
                            else -> createClozeExercise(
                                title = "Cloze ${randomString(random, 8)}",
                                language = "javascript",
                                content = "const ___ = ___",
                                correctValuesByBlank = listOf(
                                    listOf(randomString(random, 5)),
                                    listOf("'${randomString(random, 5)}'")
                                ),
                                options = listOf(
                                    randomString(random, 5),
                                    "'${randomString(random, 5)}'",
                                    "let",
                                    "const"
                                )
                            )
                        }
                    }
                }

                "DELETE_FIRST" -> if (list.size > 1) list.removeAt(0)
                "DELETE_LAST" -> if (list.size > 1) list.removeAt(list.size - 1)
                "DELETE_MIDDLE" -> if (list.size > 2) list.removeAt(list.size / 2)
                "DELETE_RANDOM" -> if (list.size > 1) list.removeAt(random.nextInt(list.size))

                "REORDER" -> {
                    if (list.size > 1) {
                        val shuffled = list.toMutableList().apply { shuffle(random) }
                        list.clear()
                        list.addAll(shuffled)
                    }
                }

                "CHANGE_KIND" -> {
                    if (list.isNotEmpty()) {
                        val i = random.nextInt(list.size)
                        val old = list[i]
                        val newEx = when (random.nextInt(3)) {
                            0 -> createInfoExercise("Info ${randomString(random, 8)}")
                            1 -> createSelectExercise(
                                title = old.blocks.filterIsInstance<HeaderBlock>().firstOrNull()?.content ?: "Select",
                                correctValue = randomString(random, 6),
                                distractors = arrayOf(randomString(random, 6))
                            )
                            else -> createClozeExercise(
                                title = old.blocks.filterIsInstance<HeaderBlock>().firstOrNull()?.content ?: "Cloze",
                                language = "python",
                                content = "print(___)",
                                correctValuesByBlank = listOf(listOf("\"hi\"", "'hi'")),
                                options = listOf("\"hi\"", "'hi'", "hi")
                            )
                        }
                        list[i] = newEx.copy(exerciseId = old.exerciseId)
                    }
                }
            }
        }

        return list
    }

    private fun randomString(random: Random, length: Int): String {
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { chars[random.nextInt(chars.size)] }
            .joinToString("")
    }
}