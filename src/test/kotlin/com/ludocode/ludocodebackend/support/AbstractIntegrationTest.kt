package com.ludocode.ludocodebackend.support
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Exercise
import com.ludocode.ludocodebackend.catalog.domain.entity.ExerciseOption
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.LessonExercises
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLessons
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.catalog.infra.repository.*
import com.ludocode.ludocodebackend.progress.infra.repository.AttemptOptionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import com.ludocode.ludocodebackend.progress.infra.repository.ExerciseAttemptRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import com.ludocode.ludocodebackend.progress.infra.repository.UserStatsRepository
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
import java.util.UUID


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
abstract class AbstractIntegrationTest {


    lateinit var python: Course
    lateinit var swift: Course
    lateinit var pyMod1: Module
    lateinit var pyMod2: Module
    lateinit var swMod1: Module
    lateinit var sw1Lessons: List<Lesson>
    lateinit var py1Lessons: List<Lesson>
    lateinit var py2Lessons: List<Lesson>
    lateinit var exercises: List<Exercise>
    lateinit var py1Lesson1Exercises: List<Exercise>
    lateinit var py1Lesson2Exercises: List<Exercise>



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

    protected fun initializeUsers () {



        user1 = userRepository.save(
            User(firstName = "John", lastName = "Doe", pfpSrc = "Test", createdAt = OffsetDateTime.now(), email = "email@google.com"))
    }

    @Transactional
    fun initializeCatalog() {
        // Courses
        python = courseRepository.save(Course(id = UUID.randomUUID(), title = "Python"))
        swift  = courseRepository.save(Course(id = UUID.randomUUID(), title = "Swift"))

        // Modules (order on module itself)
         pyMod1 = moduleRepository.save(
            Module(id = UUID.randomUUID(), title = "Variables",   courseId = python.id, orderIndex = 1, isDeleted = false)
        )
        pyMod2 = moduleRepository.save(
            Module(id = UUID.randomUUID(), title = "Conditionals", courseId = python.id, orderIndex = 2, isDeleted = false)
        )
        swMod1 = moduleRepository.save(
            Module(id = UUID.randomUUID(), title = "Variables",   courseId = swift.id,  orderIndex = 1, isDeleted = false)
        )

        py1Lessons = saveLessons(
            "Variables I",
            "Variables II",
            "Data Types I",
            "Data Types II"
        )

        py2Lessons = saveLessons(
            "If",
            "Else",
            "Else if",
            "Switch"
        )

        sw1Lessons = saveLessons(
            "Variables I",
            "Variables II",
            "Data Types I",
            "Data Types II"
        )

        joinLessons(pyMod1.id!!, py1Lessons)
        joinLessons(pyMod2.id!!, py2Lessons)
        joinLessons(swMod1.id!!, sw1Lessons)

        exercises = exerciseRepository.saveAll(
            listOf(
                Exercise(
                    exerciseId = ExerciseId(UUID.randomUUID(), 1),
                    title = "Complete the expression",
                    prompt = "let sum = ___ + 4",
                    exerciseType = ExerciseType.CLOZE,
                ),
                Exercise(
                    exerciseId = ExerciseId(UUID.randomUUID(), 1),
                    title = "Create a variable with a value of 'House'",
                    prompt = "const ___ = ___",
                    exerciseType = ExerciseType.CLOZE,
                ),
                Exercise(
                    exerciseId = ExerciseId(UUID.randomUUID(), 1),
                    title = "What will the following code return",
                    prompt = "const score = 4 + 4;",
                    exerciseType = ExerciseType.ANALYZE,
                ),
                Exercise(
                    exerciseId = ExerciseId(UUID.randomUUID(), 1),
                    title = "Which of the following declares a variable that can not be reassigned",
                    exerciseType = ExerciseType.TRIVIA,
                )
            )
        )

        val lessonExercises = lessonExercisesRepository.saveAll(
            listOf(
                LessonExercises(LessonExercisesId(py1Lessons[0].id!!, 1), exercises[0].exerciseId.id!!, 1),
                LessonExercises(LessonExercisesId(py1Lessons[0].id!!, 2), exercises[1].exerciseId.id!!, 1),
                LessonExercises(LessonExercisesId(py2Lessons[1].id!!, 1), exercises[2].exerciseId.id!!, 1),
                LessonExercises(LessonExercisesId(py2Lessons[1].id!!, 2), exercises[3].exerciseId.id!!, 1)
            )
        )

        py1Lesson1Exercises = listOf(exercises[0], exercises[1])
        py1Lesson2Exercises = listOf(exercises[2], exercises[3])

        val optionContents = listOf("4", "house", "'house'", "8", "undefined", "let", "const")
        optionContents.forEach { optionContentRepository.upsertOption(it) }
        val dbOptions = optionContents.mapNotNull { optionContentRepository.findOptionContentByContent(it) }

        val exerciseOptions = exerciseOptionRepository.saveAll(
            listOf(
                // Ex 1 (CLOZE): correct "4" → order 1; distractor "let" → null
                ExerciseOption(UUID.randomUUID(), exercises[0].exerciseId.id!!, 1, dbOptions[0].id, 1),
                ExerciseOption(UUID.randomUUID(), exercises[0].exerciseId.id!!, 1, dbOptions[5].id, null),

                // Ex 2 (CLOZE): two corrects → "house"=1, "'house'"=2
                ExerciseOption(UUID.randomUUID(), exercises[1].exerciseId.id!!, 1, dbOptions[1].id, 1),
                ExerciseOption(UUID.randomUUID(), exercises[1].exerciseId.id!!, 1, dbOptions[2].id, 2),

                // Ex 3 (ANALYZE): correct "8" → 1; distractor "undefined" → null
                ExerciseOption(UUID.randomUUID(), exercises[2].exerciseId.id!!, 1, dbOptions[3].id, 1),
                ExerciseOption(UUID.randomUUID(), exercises[2].exerciseId.id!!, 1, dbOptions[4].id, null),

                // Ex 4 (TRIVIA): correct "const" → 1; distractor "let" → null
                ExerciseOption(UUID.randomUUID(), exercises[3].exerciseId.id!!, 1, dbOptions[6].id, 1),
                ExerciseOption(UUID.randomUUID(), exercises[3].exerciseId.id!!, 1, dbOptions[5].id, null),
            )
        )



    }

    private fun saveLessons(vararg titles: String): List<Lesson> =
        lessonRepository.saveAll(
            titles.map {
                Lesson(
                    id = UUID.randomUUID(),
                    title = it,
                    isDeleted = false
                )
            }
        )

    private fun joinLessons(moduleId: UUID, lessons: List<Lesson>) {
        moduleLessonsRepository.saveAll(
            lessons.mapIndexed { idx, lesson ->
                ModuleLessons(
                    moduleLessonsId = ModuleLessonsId(
                        moduleId = moduleId,
                        orderIndex = idx + 1
                    ),
                    lessonId = lesson.id!!
                )
            }
        )
    }


}