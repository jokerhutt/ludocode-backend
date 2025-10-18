package com.ludocode.ludocodebackend.support
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.domain.entity.Lesson
import com.ludocode.ludocodebackend.catalog.domain.entity.Module
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.LessonRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.ModuleRepository
import com.ludocode.ludocodebackend.progress.infra.LessonCompletionRepository
import com.ludocode.ludocodebackend.user.domain.entity.User
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.OffsetDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig::class)
abstract class AbstractIntegrationTest {

    @Autowired
    private lateinit var userRepository: UserRepository
    lateinit var pythonCourse: Course
    lateinit var pyModule1: Module
    lateinit var pyModule2: Module
    lateinit var pyModule1Lessons: List<Lesson>
    lateinit var pyModule2Lessons: List<Lesson>

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

    @Autowired lateinit var courseRepository: CourseRepository
    @Autowired lateinit var lessonRepository: LessonRepository
    @Autowired lateinit var moduleRepository: ModuleRepository
    @Autowired lateinit var lessonCompletionRepository: LessonCompletionRepository

    @Autowired
    protected lateinit var jdbc: JdbcTemplate

    @BeforeEach
    fun restAssuredBase() {
        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
    }

    @BeforeEach
    fun resetDb() {
        jdbc.execute(
            """
        TRUNCATE TABLE 
          lesson_completion,
          lesson, 
          module, 
          course, 
          ludo_user
        RESTART IDENTITY CASCADE
        """.trimIndent()
        )

        initializeCatalog()
        initializeUsers()

    }

    protected fun initializeUsers () {
        user1 = userRepository.save(
            User(firstName = "John", lastName = "Doe", pfpSrc = "Test", createdAt = OffsetDateTime.now(), currentCourse = pythonCourse.id, email = "email@google.com"))
    }

    protected fun initializeCatalog () {

        pythonCourse = courseRepository.save(Course(title = "Python"))
        pyModule1 = moduleRepository.save(Module(title = "Variables", courseId = pythonCourse.id, orderIndex = 1))
        pyModule2 = moduleRepository.save(Module(title = "Conditionals", courseId = pythonCourse.id, orderIndex = 2))

        pyModule1Lessons = lessonRepository.saveAll(
            listOf(
            Lesson(title = "Variables I", moduleId = pyModule1.id, orderIndex = 1),
            Lesson(title = "Variables II", moduleId = pyModule1.id, orderIndex = 2),
            Lesson(title = "Data Types I", moduleId = pyModule1.id, orderIndex = 3),
            Lesson(title = "Data Types II", moduleId = pyModule1.id, orderIndex = 4),
                )
        )

        pyModule2Lessons = lessonRepository.saveAll(
            listOf(
                Lesson(title = "If", moduleId = pyModule2.id, orderIndex = 1),
                Lesson(title = "Else", moduleId = pyModule2.id, orderIndex = 2),
                Lesson(title = "Else if", moduleId = pyModule2.id, orderIndex = 3),
                Lesson(title = "Switch", moduleId = pyModule2.id, orderIndex = 4),
            )
        )

    }


}