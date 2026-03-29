package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlLesson
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlModule
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlRoot
import com.ludocode.ludocodebackend.catalog.domain.enums.CourseType
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.lesson.api.dto.snapshot.ExerciseSnap
import com.ludocode.ludocodebackend.lesson.domain.enums.LessonType
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ClozeInteraction
import com.ludocode.ludocodebackend.lesson.domain.jsonb.HeaderBlock
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionBlank
import com.ludocode.ludocodebackend.lesson.domain.jsonb.InteractionFile
import com.ludocode.ludocodebackend.lesson.domain.jsonb.ParagraphBlock
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.CourseSnap
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import kotlin.test.Test

class CreateCourseIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun createCourse_createsCourse_returnsNewCourses() {

        val newCourseName = "Python New"
        val requestHash = UUID.randomUUID()

        val req = CreateCourseRequest(newCourseName, requestHash, "New python course that is awesome", CourseType.COURSE, "Star", pythonLanguage)

        val res = submitPostCreateCourse(req)

        assertThat(res).isNotNull()
        assertThat(res.size).isEqualTo(3)

        val created = res.single { it.title == newCourseName }

        assertThat(created).isNotNull()
        assertThat(created.title).isEqualTo(newCourseName)
        assertThat(created.codeLanguage).isEqualTo(pythonLanguage)
    }

    @Test
    fun submitYamlCreateCourse_createsCourse() {

        val yamlReq = CurriculumYamlRoot(
            title = "Python YAML Course",
            courseIcon = "Star",
            language = pythonLanguage,
            description = "Cool Python Stuff",
            courseType = CourseType.COURSE,
            modules = listOf(
                CurriculumYamlModule(
                    id = null,
                    title = "Printing stuff to the console",
                    lessons = listOf(
                        CurriculumYamlLesson(
                            id = null,
                            title = "Print Statements",
                            lessonType = LessonType.NORMAL,
                            exercises = listOf(

                                // INFO
                                ExerciseSnap(
                                    exerciseId = UUID.randomUUID(),
                                    blocks = listOf(
                                        HeaderBlock("Printing in Python"),
                                        ParagraphBlock("Use print() to output text.")
                                    ),
                                    interaction = null
                                ),

                                // CLOZE
                                ExerciseSnap(
                                    exerciseId = UUID.randomUUID(),
                                    blocks = listOf(
                                        HeaderBlock("Fill in the missing function"),
                                        ParagraphBlock("Complete the code.")
                                    ),
                                    interaction = ClozeInteraction(
                                        file = InteractionFile(
                                            language = "python",
                                            content = """
___("Hello world")
""".trimIndent()
                                        ),
                                        blanks = listOf(
                                            InteractionBlank(
                                                index = 0,
                                                correctOptions = listOf("print")
                                            )
                                        ),
                                        options = listOf("print", "echo", "log"),
                                        output = "Hello world"
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )

        submitPostCreateCourseWithYaml(yamlReq)

        val pythonSnap = buildCourseSnapshotByTitle("Python YAML Course")

        assertThat(pythonSnap.title).isEqualTo("Python YAML Course")

        assertThat(pythonSnap.modules.size).isEqualTo(1)
        assertThat(pythonSnap.modules[0].lessons.size).isEqualTo(1)
        assertThat(pythonSnap.modules[0].lessons[0].exercises.size).isEqualTo(2)
    }

    fun buildCourseSnapshotByTitle(title: String): CourseSnap {
        val course = courseRepository.findByTitle(title)
            ?: throw IllegalStateException("Course not found")

        return testSnapshotService.buildCourseSnapshot(course.id)
    }

    private fun submitPostCreateCourse(req: CreateCourseRequest): List<CourseResponse> =
        TestRestClient
            .postOk(
                "${ApiPaths.SNAPSHOTS.ADMIN_BASE}${ApiPaths.SNAPSHOTS.COURSE}",
                user1.id,
                req,
                Array<CourseResponse>::class.java
            )
            .toList()

    private fun submitPostCreateCourseWithYaml(req: CurriculumYamlRoot) {
        TestRestClient.postNoContentYaml(
            "${ApiPaths.SNAPSHOTS.ADMIN_BASE}${ApiPaths.SNAPSHOTS.COURSE}?mode=yaml",
            user1.id,
            req,
        )
    }


}