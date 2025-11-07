package com.ludocode.ludocodebackend.support
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CourseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.LessonExercises
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLessons
import com.ludocode.ludocodebackend.catalog.domain.entity.OptionContent
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.catalog.infra.repository.*
import com.ludocode.ludocodebackend.progress.infra.repository.AttemptOptionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import com.ludocode.ludocodebackend.progress.infra.repository.ExerciseAttemptRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserDailyGoalRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserStatsRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserStreakRepository
import com.ludocode.ludocodebackend.user.domain.entity.User
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import io.restassured.RestAssured
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.OffsetDateTime
import java.time.Clock
import java.util.UUID


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")

@Import(TestSecurityConfig::class, FixedClockConfig::class)
abstract class AbstractIntegrationTest {


     var pythonId = UUID.randomUUID()
     var swiftId  = UUID.randomUUID()

     var pyMod1Id = UUID.randomUUID()
     var pyMod2Id = UUID.randomUUID()
     var swMod1Id = UUID.randomUUID()

     var py1L1 = UUID.randomUUID()
     var py1L2 = UUID.randomUUID()
     var py1L3 = UUID.randomUUID()
     var py1L4 = UUID.randomUUID()

     var py2L1 = UUID.randomUUID()
     var py2L2 = UUID.randomUUID()
     var py2L3 = UUID.randomUUID()
     var py2L4 = UUID.randomUUID()

     var sw1L1 = UUID.randomUUID()
     var sw1L2 = UUID.randomUUID()
     var sw1L3 = UUID.randomUUID()
     var sw1L4 = UUID.randomUUID()

    lateinit var user1: User


    init {
        Containers.POSTGRES.isRunning
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun postgresProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { Containers.POSTGRES.jdbcUrl }
            registry.add("spring.datasource.username") { Containers.POSTGRES.username }
            registry.add("spring.datasource.password") { Containers.POSTGRES.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }

        @JvmStatic
        @BeforeAll
        fun restAssuredLogging() {
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
        }
    }

    @LocalServerPort
    protected var port: Int = 0

    @Autowired
    lateinit var clock: Clock
    @Autowired lateinit var courseProgressRepository: CourseProgressRepository
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var courseRepository: CourseRepository
    @Autowired lateinit var lessonRepository: LessonRepository
    @Autowired lateinit var moduleRepository: ModuleRepository
    @Autowired lateinit var exerciseRepository: ExerciseRepository
    @Autowired lateinit var lessonCompletionRepository: LessonCompletionRepository
    @Autowired lateinit var moduleLessonsRepository: ModuleLessonsRepository
    @Autowired lateinit var lessonExercisesRepository: LessonExercisesRepository
    @Autowired lateinit var exerciseOptionRepository: ExerciseOptionRepository
    @Autowired lateinit var optionContentRepository: OptionContentRepository
    @Autowired lateinit var userStatsRepository: UserStatsRepository
    @Autowired lateinit var exerciseAttemptRepository: ExerciseAttemptRepository
    @Autowired lateinit var attemptOptionRepository: AttemptOptionRepository
    @Autowired lateinit var userStreakRepository: UserStreakRepository
    @Autowired lateinit var userDailyGoalRepository: UserDailyGoalRepository

    @Autowired
    protected lateinit var jdbc: JdbcTemplate

    @BeforeEach
    fun restAssuredBase() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = 18080
    }

    @BeforeEach
    fun resetDb() {
        jdbc.execute(
            """
        TRUNCATE TABLE 
          attempt_option,
          exercise_attempt,
          user_daily_goal,
          user_streak,
          lesson_completion,
          course_progress,
          user_stats,
          ludo_user,
          option_content,
          exercise_option,
          lesson_exercises,
          module_lessons,
          exercise,
          exercise, 
          module, 
          course
        RESTART IDENTITY CASCADE
        """.trimIndent()
        )

        initializeCatalog()
        initializeUsers()

    }

    @Transactional
    fun importSnapshots(snaps: List<CourseSnap>, defaultVersion: Int = 1) {
        // 0) Validate snapshot invariants early if you want
        //    e.g., cloze gap count == number of correct options, etc.

        // 1) Preload all OptionContent in bulk to avoid per-row lookups
        val allContents = snaps.flatMap { it.modules }
            .flatMap { it.lessons }
            .flatMap { it.exercises }
            .flatMap { ex -> ex.correctOptions.map { it.content } + ex.distractors.map { it.content } }
            .toSet()

        val existing = optionContentRepository.findAllByContentIn(allContents)
        val contentIdByText = existing.associate { it.content to it.id }.toMutableMap()

        val missing = allContents - contentIdByText.keys
        if (missing.isNotEmpty()) {
            val toSave = missing.map { OptionContent(id = UUID.randomUUID(), content = it) }
            optionContentRepository.saveAll(toSave)
            toSave.forEach { contentIdByText[it.content] = it.id }
        }

        // 2) Upsert courses, modules, lessons, exercises
        snaps.forEach { cs ->
            courseRepository.save(Course(id = cs.courseId, title = cs.title /* or from elsewhere */))

            cs.modules.forEachIndexed { mIdx, ms ->
                moduleRepository.save(
                    Module(
                        id = ms.moduleId,
                        title = ms.title,
                        courseId = cs.courseId,
                        orderIndex = mIdx + 1,
                        isDeleted = false
                    )
                )

                ms.lessons.forEach { ls ->
                    lessonRepository.save(Lesson(id = ls.id, title = ls.title, isDeleted = false))

                    // module ↔ lesson join
                    moduleLessonsRepository.save(
                        ModuleLessons(
                            moduleLessonsId = ModuleLessonsId(
                                moduleId = ms.moduleId,
                                orderIndex = ls.orderIndex
                            ),
                            lessonId = ls.id
                        )
                    )

                    // exercises
                    ls.exercises.forEachIndexed { exIdx, ex ->
                        val exId = ExerciseId(ex.id, defaultVersion)
                        exerciseRepository.save(
                            Exercise(
                                exerciseId = exId,
                                title = ex.title,
                                subtitle = ex.subtitle,
                                prompt = ex.prompt,
                                exerciseMedia = ex.media,             // if your entity supports it
                                exerciseType = ex.exerciseType,
                                isDeleted = false
                            )
                        )

                        // lesson ↔ exercise join (order = list index)
                        lessonExercisesRepository.save(
                            LessonExercises(
                                LessonExercisesId(lessonId = ls.id, orderIndex = exIdx + 1),
                                exerciseId = ex.id,
                                exerciseVersion = defaultVersion
                            )
                        )
                    }
                }
            }
        }

        // 3) Exercise options after exercises exist
        snaps.forEach { cs ->
            cs.modules.forEach { ms ->
                ms.lessons.forEach { ls ->
                    ls.exercises.forEach { ex ->
                        // correct
                        ex.correctOptions.forEachIndexed { i, opt ->
                            exerciseOptionRepository.save(
                                ExerciseOption(
                                    id = opt.exerciseOptionId,
                                    exerciseId = ex.id,
                                    exerciseVersion = defaultVersion,
                                    optionId = contentIdByText.getValue(opt.content),
                                    answerOrder = opt.answerOrder ?: (i + 1)
                                )
                            )
                        }
                        // distractors
                        ex.distractors.forEach { opt ->
                            exerciseOptionRepository.save(
                                ExerciseOption(
                                    id = opt.exerciseOptionId,
                                    exerciseId = ex.id,
                                    exerciseVersion = defaultVersion,
                                    optionId = contentIdByText.getValue(opt.content),
                                    answerOrder = null
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    protected fun initializeUsers () {



        user1 = userRepository.save(
            User(firstName = "John", lastName = "Doe", pfpSrc = "Test", createdAt = OffsetDateTime.now(clock), email = "email@google.com"))
    }

    @Transactional
    fun initializeCatalog() {
        // ---- IDs


        // ---- Exercises (default version handled inside importSnapshots)
        val ex1 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "Complete the expression",
            subtitle = null,
            prompt = "let sum = ___ + 4",
            media = null,
            exerciseType = ExerciseType.CLOZE,
            correctOptions = listOf(
                OptionSnap(content = "4", answerOrder = 1, UUID.randomUUID())
            ),
            distractors = listOf(
                OptionSnap(content = "let", answerOrder = null, UUID.randomUUID())
            )
        )

        val ex2 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "Create a variable with a value of 'House'",
            subtitle = null,
            prompt = "const ___ = ___",
            media = null,
            exerciseType = ExerciseType.CLOZE,
            correctOptions = listOf(
                OptionSnap(content = "house",  answerOrder = 1, UUID.randomUUID()),
                OptionSnap(content = "'house'", answerOrder = 2, UUID.randomUUID())
            ),
            distractors = emptyList()
        )

        val ex3 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "What will the following code return",
            subtitle = null,
            prompt = "const score = 4 + 4;",
            media = null,
            exerciseType = ExerciseType.ANALYZE,
            correctOptions = listOf(
                OptionSnap(content = "8", answerOrder = 1, UUID.randomUUID())
            ),
            distractors = listOf(
                OptionSnap(content = "undefined", answerOrder = null, UUID.randomUUID())
            )
        )

        val ex4 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "Which of the following declares a variable that can not be reassigned",
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.TRIVIA,
            correctOptions = listOf(
                OptionSnap(content = "const", answerOrder = 1, UUID.randomUUID())
            ),
            distractors = listOf(
                OptionSnap(content = "let", answerOrder = null, UUID.randomUUID())
            )
        )

        val ex5 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "What will this print?",
            subtitle = null,
            prompt = "print(2 == 2)",
            media = null,
            exerciseType = ExerciseType.ANALYZE,
            correctOptions = listOf(
                OptionSnap(content = "True", answerOrder = 1, UUID.randomUUID())
            ),
            distractors = listOf(
                OptionSnap(content = "False", answerOrder = null, UUID.randomUUID())
            )
        )

        val ex6 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "Complete the expression",
            subtitle = null,
            prompt = "___ i == 5",
            media = null,
            exerciseType = ExerciseType.CLOZE,
            correctOptions = listOf(
                OptionSnap(content = "if", answerOrder = 1, UUID.randomUUID())
            ),
            distractors = listOf(
                OptionSnap(content = "let", answerOrder = null, UUID.randomUUID())
            )
        )

        val ex7 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "Complete the expression",
            subtitle = null,
            prompt = "if i ___ 4",
            media = null,
            exerciseType = ExerciseType.CLOZE,
            correctOptions = listOf(
                OptionSnap(content = "==", answerOrder = 1, UUID.randomUUID())
            ),
            distractors = listOf(
                OptionSnap(content = "===", answerOrder = null, UUID.randomUUID())
            )
        )

        val ex8 = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "Complete the expression",
            subtitle = null,
            prompt = "for i ___ points",
            media = null,
            exerciseType = ExerciseType.CLOZE,
            correctOptions = listOf(
                OptionSnap(content = "in", answerOrder = 1, UUID.randomUUID())
            ),
            distractors = listOf(
                OptionSnap(content = "while", answerOrder = null, UUID.randomUUID())
            )
        )

        val pyMod1Lessons = listOf(
            LessonSnap(
                id = py1L1, title = "Variables I", orderIndex = 1,
                exercises = listOf(ex1, ex2)
            ),
            LessonSnap(id = py1L2, title = "Variables II", orderIndex = 2, exercises = listOf(ex3, ex4)),
            LessonSnap(id = py1L3, title = "Data Types I", orderIndex = 3, exercises = listOf(ex5)),
            LessonSnap(id = py1L4, title = "Data Types II", orderIndex = 4, exercises = listOf(ex6, ex7))
        )

        val pyMod2Lessons = listOf(
            LessonSnap(id = py2L1, title = "If",       orderIndex = 1, exercises = listOf(ex7)),
            LessonSnap(
                id = py2L2, title = "Else",     orderIndex = 2,
                exercises = listOf(ex8)
            ),
        )

        val swMod1Lessons = listOf(
            LessonSnap(id = sw1L1, title = "Variables I", orderIndex = 1, exercises = emptyList()),
            LessonSnap(id = sw1L2, title = "Variables II", orderIndex = 2, exercises = emptyList()),
            LessonSnap(id = sw1L3, title = "Data Types I", orderIndex = 3, exercises = emptyList()),
            LessonSnap(id = sw1L4, title = "Data Types II", orderIndex = 4, exercises = emptyList())
        )

        // ---- Modules
        val pythonModules = listOf(
            ModuleSnapshot(moduleId = pyMod1Id, title = "Variables",   lessons = pyMod1Lessons),
            ModuleSnapshot(moduleId = pyMod2Id, title = "Conditionals", lessons = pyMod2Lessons)
        )
        val swiftModules = listOf(
            ModuleSnapshot(moduleId = swMod1Id, title = "Variables", lessons = swMod1Lessons)
        )

        // ---- Courses
        val snaps = listOf(
            CourseSnap(courseId = pythonId, title = "Python", modules = pythonModules),
            CourseSnap(courseId = swiftId,  title = "Swift",  modules = swiftModules)
        )

        // Single transactional import
        importSnapshots(snaps, defaultVersion = 1)
    }


}