package com.ludocode.ludocodebackend.support

import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Subject
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.SubjectRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.config.*
import com.ludocode.ludocodebackend.config.security.TestSecurityConfig
import com.ludocode.ludocodebackend.config.time.MutableClock
import com.ludocode.ludocodebackend.config.time.TestClockConfig
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.OptionSnap
import com.ludocode.ludocodebackend.lesson.domain.entity.*
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import com.ludocode.ludocodebackend.lesson.infra.repository.*
import com.ludocode.ludocodebackend.playground.infra.repository.ProjectFileRepository
import com.ludocode.ludocodebackend.playground.infra.repository.UserProjectRepository
import com.ludocode.ludocodebackend.preferences.api.infra.repository.CareerPreferencesRepository
import com.ludocode.ludocodebackend.preferences.domain.entity.CareerPreference
import com.ludocode.ludocodebackend.preferences.domain.entity.UserPreferences
import com.ludocode.ludocodebackend.progress.infra.repository.*
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.support.snapshot.CourseSnap
import com.ludocode.ludocodebackend.support.snapshot.ModuleSnap
import com.ludocode.ludocodebackend.support.snapshot.SubjectSnap
import com.ludocode.ludocodebackend.user.domain.entity.ExternalAccount
import com.ludocode.ludocodebackend.user.domain.entity.User
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import com.ludocode.ludocodebackend.user.infra.repository.ExternalAccountRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import io.restassured.RestAssured
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Import(
    TestSecurityConfig::class, TestClockConfig::class, GcpTestConfig::class, GeminiTestConfig::class,
    FirebaseAuthTestConfig::class, TestCacheConfig::class, StripeTestConfig::class
)
abstract class AbstractIntegrationTest {


    @Autowired
    private lateinit var careerPreferencesRepository: CareerPreferencesRepository

    @Autowired
    private lateinit var languagesMapper: LanguagesMapper

    @Autowired
    private lateinit var subjectRepository: SubjectRepository

    @Autowired
    private lateinit var codeLanguagesRepository: CodeLanguagesRepository
    var pythonId = UUID.randomUUID()
    var swiftId = UUID.randomUUID()

    var pyMod1Id = UUID.randomUUID()
    var pyMod2Id = UUID.randomUUID()
    var swMod1Id = UUID.randomUUID()
    var swMod2Id = UUID.randomUUID()

    var py1L1 = UUID.randomUUID()
    var py1L2 = UUID.randomUUID()
    var py1L3 = UUID.randomUUID()
    var py1L4 = UUID.randomUUID()

    var py2L1 = UUID.randomUUID()
    var py2L2 = UUID.randomUUID()

    var sw1L1 = UUID.randomUUID()
    var sw1L2 = UUID.randomUUID()
    var sw1L3 = UUID.randomUUID()
    var sw1L4 = UUID.randomUUID()

    var pythonLessons = listOf(
        py1L1, py1L2, py1L3, py1L4, py2L1, py2L2
    )

    lateinit var user1: User
    lateinit var user2: User
    lateinit var demoUser1: User
    val demoToken: String = "9d495788fdc9fe95627f04ab32cc839e"

    lateinit var pythonLanguage: CodeLanguages
    lateinit var swiftLanguage: CodeLanguages
    lateinit var luaLanguage: CodeLanguages
    lateinit var jsLanguage: CodeLanguages

    lateinit var pythonSubject: Subject
    lateinit var swiftSubject: Subject

    lateinit var dataPath: CareerPreference


    init {
        Containers.POSTGRES.isRunning
        Containers.FAKE_GCS.isRunning
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun postgresProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { Containers.POSTGRES.jdbcUrl }
            registry.add("spring.datasource.username") { Containers.POSTGRES.username }
            registry.add("spring.datasource.password") { Containers.POSTGRES.password }
            registry.add("app.gcs.host") {
                "http://localhost:${Containers.FAKE_GCS.getMappedPort(4443)}"
            }
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
    lateinit var clock: MutableClock
    @Autowired
    lateinit var courseProgressRepository: CourseProgressRepository
    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var externalAccountRepository: ExternalAccountRepository
    @Autowired
    lateinit var courseRepository: CourseRepository
    @Autowired
    lateinit var lessonRepository: LessonRepository
    @Autowired
    lateinit var moduleRepository: ModuleRepository
    @Autowired
    lateinit var exerciseRepository: ExerciseRepository
    @Autowired
    lateinit var lessonCompletionRepository: LessonCompletionRepository
    @Autowired
    lateinit var moduleLessonsRepository: ModuleLessonsRepository
    @Autowired
    lateinit var lessonExercisesRepository: LessonExercisesRepository
    @Autowired
    lateinit var exerciseOptionRepository: ExerciseOptionRepository
    @Autowired
    lateinit var optionContentRepository: OptionContentRepository
    @Autowired
    lateinit var userCoinsRepository: UserCoinsRepository
    @Autowired
    lateinit var exerciseAttemptRepository: ExerciseAttemptRepository
    @Autowired
    lateinit var attemptOptionRepository: AttemptOptionRepository
    @Autowired
    lateinit var userStreakRepository: UserStreakRepository
    @Autowired
    lateinit var userDailyGoalRepository: UserDailyGoalRepository
    @Autowired
    lateinit var userProjectRepository: UserProjectRepository
    @Autowired
    lateinit var projectFileRepository: ProjectFileRepository
    @Autowired
    lateinit var subscriptionPlanRepository: SubscriptionPlanRepository
    @Autowired
    lateinit var userSubscriptionRepository: UserSubscriptionRepository


    @Autowired
    lateinit var storage: Storage

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
          project_file,
          user_project,
          attempt_option,
          exercise_attempt,
          user_daily_goal,
          user_streak,
          lesson_completion,
          course_progress,
          user_coins,
          external_account,
          user_subscription,
          ludo_user,
          option_content,
          exercise_option,
          lesson_exercises,
          module_lessons,
          lesson,
          lesson, 
          module, 
          course,
          subjects,
          code_languages,
          subscription_plan,
          user_preferences
        RESTART IDENTITY CASCADE
        """.trimIndent()
        )

        initializeLanguages()
        initializeSubjects()
        initializeCatalog()
        initializeCareerPaths()
        initializeUsers()

    }

    @Transactional
    fun importSnapshots(snaps: List<CourseSnap>, defaultVersion: Int = 1) {


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

            val subject = subjectRepository.findBySlugAndName(cs.courseSubject.slug, cs.courseSubject.name)
            val language =
                cs.language?.languageId?.let { id ->
                    codeLanguagesRepository.findByIdOrNull(id)
                        ?: throw ApiException(ErrorCode.LANGUAGE_NOT_FOUND)
                }

            courseRepository.save(
                Course(
                    id = cs.courseId,
                    title = cs.title,
                    courseType = cs.courseType,
                    subject = subject!!,
                    language = language,
                    description = "cool course"
                )
            )

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
                        ModuleLesson(
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
                                exerciseMedia = ex.media,
                                exerciseType = ex.exerciseType,
                                isDeleted = false
                            )
                        )

                        // lesson ↔ lesson join (order = list index)
                        lessonExercisesRepository.save(
                            LessonExercise(
                                LessonExercisesId(lessonId = ls.id, orderIndex = exIdx + 1),
                                exerciseId = ex.id,
                                exerciseVersion = defaultVersion
                            )
                        )
                    }
                }
            }
        }

        snaps.forEach { cs ->
            cs.modules.forEach { ms ->
                ms.lessons.forEach { ls ->
                    ls.exercises.forEach { ex ->
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

    protected fun initializeUsers() {
        user1 = userRepository.save(
            User(displayName = "John Doe", createdAt = OffsetDateTime.now(clock), email = "email@google.com", stripeCustomerId = "cus_1")
        )
        user2 = userRepository.save(
            User(displayName = "Micheal Scott", createdAt = OffsetDateTime.now(clock), email = "mscott@google.com")
        )

        demoUser1 = userRepository.save(
            User(
                id = UUID.fromString("47ad6daf-2433-4e76-b9c1-305614c5c033"),
                displayName = "Demo User",
                email = "demoUser",
                createdAt = OffsetDateTime.now(clock),
                stripeCustomerId = "cus_test"
            )
        )

        externalAccountRepository.save(
            ExternalAccount(
                userId = user1.id!!,
                provider = AuthProvider.FIREBASE,
                providerUserId = MockOauthConstants.USER_1_GOOGLE_SUB,
                createdAt = Instant.from(OffsetDateTime.now(clock))
            )
        )

        externalAccountRepository.save(
            ExternalAccount(
                userId = demoUser1.id!!,
                provider = AuthProvider.DEMO,
                providerUserId = demoUser1.id.toString(),
                createdAt = Instant.from(OffsetDateTime.now(clock))
            )
        )


    }

    protected fun initializeCareerPaths() {
        dataPath = careerPreferencesRepository.save(
            CareerPreference(choice = "DATA", title = "Data Science", description =  "Data science and stuff", courseId = pythonId)
        )
    }

    @Transactional
    fun initializeLanguages() {
        pythonLanguage = codeLanguagesRepository.save(
            CodeLanguages(
                slug = "py",
                name = "python",
                editorId = "python",
                initialScript = "print('Hello World!')",
                base = "script",
                extension = ".py",
                pistonId = "python",
                iconName = "Python"
            )
        )
        swiftLanguage = codeLanguagesRepository.save(
            CodeLanguages(
                slug = "swift",
                name = "swift",
                editorId = "swift",
                initialScript = "print('Hello World!')",
                base = "script",
                extension = ".swift",
                pistonId = "swift",
                iconName = "Swift"
            )
        )
        luaLanguage = codeLanguagesRepository.save(
            CodeLanguages(
                slug = "lua",
                name = "Lua",
                editorId = "lua",
                initialScript = "print('Hello World!')",
                base = "script",
                extension = ".lua",
                pistonId = "lua",
                iconName = "Lua"
            )
        )
        jsLanguage = codeLanguagesRepository.save(
            CodeLanguages(
                slug = "js",
                name = "Javascript",
                editorId = "js",
                initialScript = "console.log('Hello World!')",
                base = "script",
                extension = ".js",
                pistonId = "js",
                iconName = "Javascript"
            )
        )

    }

    @Transactional
    fun initializeSubjects() {
        pythonSubject = subjectRepository.save(
            Subject(
                slug = "py",
                name = "Python",
            )
        )

        swiftSubject = subjectRepository.save(
            Subject(
                slug = "swift",
                name = "swift",
            )
        )
    }

    @Transactional
    fun initializeCatalog() {

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
                OptionSnap(content = "house", answerOrder = 1, UUID.randomUUID()),
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

        val ex9INFO = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "If statements run if a condition is true",
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.INFO,
            correctOptions = emptyList(),
            distractors = emptyList()
        )

        val ex10INFO = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "They are very powerful",
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.INFO,
            correctOptions = emptyList(),
            distractors = emptyList()
        )

        val ex11INFO = ExerciseSnap(
            id = UUID.randomUUID(),
            title = "Else statements run if none of the if statement conditions were true",
            subtitle = null,
            prompt = null,
            media = null,
            exerciseType = ExerciseType.INFO,
            correctOptions = emptyList(),
            distractors = emptyList()
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
            LessonSnap(id = py2L1, title = "If", orderIndex = 1, exercises = listOf(ex7)),
            LessonSnap(
                id = py2L2, title = "Else", orderIndex = 2,
                exercises = listOf(ex8)
            ),
        )

        val swMod1Lessons = listOf(
            LessonSnap(id = sw1L1, title = "Variables I", orderIndex = 1, exercises = listOf(ex9INFO, ex10INFO)),
            LessonSnap(id = sw1L2, title = "Variables II", orderIndex = 2, exercises = listOf(ex11INFO)),
            LessonSnap(id = sw1L3, title = "Data Types I", orderIndex = 3, exercises = emptyList()),
            LessonSnap(id = sw1L4, title = "Data Types II", orderIndex = 4, exercises = emptyList())
        )


        val pythonModules = listOf(
            ModuleSnap(moduleId = pyMod1Id, title = "Variables", lessons = pyMod1Lessons),
            ModuleSnap(moduleId = pyMod2Id, title = "Conditionals", lessons = pyMod2Lessons)
        )
        val swiftModules = listOf(
            ModuleSnap(moduleId = swMod1Id, title = "Variables", lessons = swMod1Lessons),
        )

        val pythonSubjectSnap = SubjectSnap(
            slug = pythonSubject.slug,
            name = pythonSubject.name
        )

        val swiftSubjectSnap = SubjectSnap(
            slug = swiftSubject.slug,
            name = swiftSubject.name
        )

        val pythonLanguageMetadata = languagesMapper.toLanguageMetadata(pythonLanguage)
        val swiftLanguageMetadata = languagesMapper.toLanguageMetadata(swiftLanguage)

        val snaps = listOf(
            CourseSnap(
                courseId = pythonId,
                title = "Python",
                courseSubject = pythonSubjectSnap,
                courseType = CourseType.COURSE,
                modules = pythonModules,
                language = swiftLanguageMetadata
            ),
            CourseSnap(
                courseId = swiftId,
                title = "Swift",
                courseSubject = swiftSubjectSnap,
                courseType = CourseType.COURSE,
                modules = swiftModules,
                language = pythonLanguageMetadata
            )
        )

        importSnapshots(snaps, defaultVersion = 1)
    }


}