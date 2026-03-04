package com.ludocode.ludocodebackend.catalog.app.service.admin

import com.ludocode.ludocodebackend.catalog.api.dto.request.CreateCourseRequest
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.CurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonCurriculumDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.LessonDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.ModuleDraftSnapshot
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlLesson
import com.ludocode.ludocodebackend.catalog.api.dto.yaml.CurriculumYamlRoot
import com.ludocode.ludocodebackend.catalog.domain.entity.Course
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.SubjectRepository
import com.ludocode.ludocodebackend.lesson.app.service.admin.LessonSnapshotService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CurriculumYamlService(
    private val curriculumSnapshotService: CurriculumSnapshotService,
    private val lessonSnapshotService: LessonSnapshotService,
) {


    @Transactional
    fun editCourse(courseId: UUID? = null, root: CurriculumYamlRoot) {

        val resolvedCourseId = courseId ?:
            curriculumSnapshotService.createCourse(CreateCourseRequest(
                courseTitle = root.title,
                requestHash = UUID.randomUUID(),
                description = root.description,
                courseType = root.courseType,
                courseSubjectId = root.subjectId,
                languageId = root.languageId
            ))

        val lessonIdMap = mutableMapOf<CurriculumYamlLesson, UUID>()

        val snapshot = CurriculumDraftSnapshot(
            modules = root.modules.map { module ->
                ModuleDraftSnapshot(
                    id = module.id ?: UUID.randomUUID(),
                    title = module.title,
                    lessons = module.lessons.map { lesson ->

                        val lessonId = lesson.id ?: UUID.randomUUID()
                        lessonIdMap[lesson] = lessonId

                        LessonDraftSnapshot(
                            id = lessonId,
                            title = lesson.title
                        )
                    }
                )
            }
        )

        curriculumSnapshotService.applyCurriculumDiffs(resolvedCourseId, snapshot)

        root.modules.forEach { module ->
            module.lessons.forEach { lesson ->
                lessonSnapshotService.applyExercises(
                    lessonIdMap[lesson]!!,
                    LessonCurriculumDraftSnapshot(lesson.exercises)
                )
            }
        }
    }

}