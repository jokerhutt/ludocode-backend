package com.ludocode.ludocodebackend.support

import com.google.cloud.storage.Storage
import com.ludocode.ludocodebackend.analytics.infra.http.AnalyticsEventRepository
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.domain.entity.ModuleLesson
import com.ludocode.ludocodebackend.tag.domain.entity.Tag
import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ModuleLessonsId
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseStatus
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleLessonsRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.tag.infra.repository.TagRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.config.*
import com.ludocode.ludocodebackend.config.security.TestSecurityConfig
import com.ludocode.ludocodebackend.config.time.MutableClock
import com.ludocode.ludocodebackend.config.time.TestClockConfig
import com.ludocode.ludocodebackend.discussion.infra.repository.DiscussionMessageRepository
import com.ludocode.ludocodebackend.discussion.infra.repository.DiscussionRepository
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeInteraction
import com.ludocode.ludocodebackend.lesson.domain.jsonb.CodeBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.HeaderBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionBlank
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionFile
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ParagraphBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.SelectInteraction
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.languages.domain.enums.LanguageRuntime
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.LessonSnap
import com.ludocode.ludocodebackend.lesson.domain.entity.*
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.LessonExercisesId
import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.lesson.infra.repository.*
import com.ludocode.ludocodebackend.projects.infra.repository.ProjectFileRepository
import com.ludocode.ludocodebackend.projects.infra.repository.UserProjectRepository
import com.ludocode.ludocodebackend.preferences.api.infra.repository.CareerPreferencesRepository
import com.ludocode.ludocodebackend.preferences.domain.entity.CareerPreference
import com.ludocode.ludocodebackend.progress.infra.repository.*
import com.ludocode.ludocodebackend.subscription.infra.repository.SubscriptionPlanRepository
import com.ludocode.ludocodebackend.subscription.infra.repository.UserSubscriptionRepository
import com.ludocode.ludocodebackend.support.snapshot.CourseSnap
import com.ludocode.ludocodebackend.support.snapshot.ModuleSnap
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
    FirebaseAuthTestConfig::class, StripeTestConfig::class
)
abstract class AbstractIntegrationTest {


    @Autowired
    private lateinit var careerPreferencesRepository: CareerPreferencesRepository

    @Autowired
    private lateinit var languagesMapper: LanguagesMapper

    @Autowired
    private lateinit var tagRepository: TagRepository

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

    lateinit var pythonTag: Tag
    lateinit var swiftTag: Tag

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
    lateinit var userCoinsRepository: UserCoinsRepository
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
    lateinit var analyticsEventRepository: AnalyticsEventRepository
    @Autowired
    lateinit var discussionRepository: DiscussionRepository
    @Autowired
    lateinit var discussionMessageRepository: DiscussionMessageRepository

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
          discussion_message,
          discussion,
          project_like,
          project_file,
          user_project,
          exercise_attempt,
          user_daily_goal,
          user_streak,
          lesson_completion,
          course_progress,
          user_coins,
          external_account,
          user_subscription,
          ludo_user,
          lesson_exercises,
          module_lessons,
          lesson,
          module, 
          course,
          tag,
          course_tag,
          code_languages,
          subscription_plan,
          user_preferences,
          analytics_event,
          banners
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

        snaps.forEach { cs ->

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
                    courseIcon = "STAR",
                    language = language,
                    description = "cool course",
                    courseStatus = CourseStatus.PUBLISHED
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

                    lessonRepository.save(
                        Lesson(
                            id = ls.id,
                            title = ls.title,
                            lessonType = LessonType.NORMAL,
                            isDeleted = false
                        )
                    )

                    moduleLessonsRepository.save(
                        ModuleLesson(
                            moduleLessonsId = ModuleLessonsId(
                                moduleId = ms.moduleId,
                                orderIndex = ls.orderIndex
                            ),
                            lessonId = ls.id
                        )
                    )

                    ls.exercises.forEachIndexed { exIdx, ex ->

                        val exId = ExerciseId(ex.exerciseId!!, defaultVersion)

                        exerciseRepository.save(
                            Exercise(
                                exerciseId = exId,
                                blocks = ex.blocks,
                                interaction = ex.interaction
                            )
                        )

                        val exerciseId = ex.exerciseId ?: UUID.randomUUID()

                        lessonExercisesRepository.save(
                            LessonExercise(
                                LessonExercisesId(
                                    lessonId = ls.id,
                                    orderIndex = exIdx + 1
                                ),
                                exerciseId = exerciseId,
                                exerciseVersion = defaultVersion
                            )
                        )
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
                runtime = LanguageRuntime.PISTON,
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
                runtime = LanguageRuntime.PISTON,
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
                runtime = LanguageRuntime.PISTON,
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
                runtime = LanguageRuntime.PISTON,
                pistonId = "js",
                iconName = "Javascript"
            )
        )

    }

    @Transactional
    fun initializeSubjects() {
        pythonTag = tagRepository.save(
            Tag(
                slug = "py",
                name = "Python",
            )
        )

        swiftTag = tagRepository.save(
            Tag(
                slug = "swift",
                name = "swift",
            )
        )
    }

    @Transactional
    fun initializeCatalog() {

        val ex1 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("Complete the expression")
            ),
            interaction = ClozeInteraction(
                file = InteractionFile(
                    language = "javascript",
                    content = "let sum = ___ + 4"
                ),
                blanks = listOf(
                    InteractionBlank(index = 0, correctOptions = listOf("4"))
                ),
                options = listOf("4", "let")
            )
        )

        val ex2 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("Create a variable with a value of 'House'")
            ),
            interaction = ClozeInteraction(
                file = InteractionFile(
                    language = "javascript",
                    content = "const ___ = ___"
                ),
                blanks = listOf(
                    InteractionBlank(0, listOf("house")),
                    InteractionBlank(1, listOf("'house'"))
                ),
                options = listOf("house", "'house'")
            )
        )

        val ex3 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("What will the following code return"),
                CodeBlock("javascript", "const score = 4 + 4;")
            ),
            interaction = SelectInteraction(
                items = listOf("8", "undefined"),
                correctValue = "8"
            )
        )

        val ex4 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("Which of the following declares a variable that can not be reassigned")
            ),
            interaction = SelectInteraction(
                items = listOf("const", "let"),
                correctValue = "const"
            )
        )

        val ex5 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("What will this print?"),
                CodeBlock("python", "print(2 == 2)")
            ),
            interaction = SelectInteraction(
                items = listOf("True", "False"),
                correctValue = "True"
            )
        )

        val ex6 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("Complete the expression")
            ),
            interaction = ClozeInteraction(
                file = InteractionFile("python", "___ i == 5"),
                blanks = listOf(
                    InteractionBlank(0, listOf("if"))
                ),
                options = listOf("if", "let")
            )
        )

        val ex7 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("Complete the expression")
            ),
            interaction = ClozeInteraction(
                file = InteractionFile("python", "if i ___ 4"),
                blanks = listOf(
                    InteractionBlank(0, listOf("=="))
                ),
                options = listOf("==", "===")
            )
        )

        val ex8 = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                HeaderBlock("Complete the expression")
            ),
            interaction = ClozeInteraction(
                file = InteractionFile("python", "for i ___ points"),
                blanks = listOf(
                    InteractionBlank(0, listOf("in"))
                ),
                options = listOf("in", "while")
            )
        )

        val ex9INFO = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                ParagraphBlock("If statements run if a condition is true")
            ),
            interaction = null
        )

        val ex10INFO = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                ParagraphBlock("They are very powerful")
            ),
            interaction = null
        )

        val ex11INFO = ExerciseSnap(
            exerciseId = UUID.randomUUID(),
            blocks = listOf(
                ParagraphBlock("Else statements run if none of the if statement conditions were true")
            ),
            interaction = null
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

        val pythonLanguageMetadata = languagesMapper.toLanguageMetadata(pythonLanguage)
        val swiftLanguageMetadata = languagesMapper.toLanguageMetadata(swiftLanguage)

        val snaps = listOf(
            CourseSnap(
                courseId = pythonId,
                title = "Python",
                courseIcon = "Star",
                courseType = CourseType.COURSE,
                modules = pythonModules,
                language = pythonLanguageMetadata
            ),
            CourseSnap(
                courseId = swiftId,
                title = "Swift",
                courseIcon = "Star",
                courseType = CourseType.COURSE,
                modules = swiftModules,
                language = swiftLanguageMetadata
            )
        )

        importSnapshots(snaps, defaultVersion = 1)
    }


}