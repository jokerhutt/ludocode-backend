package com.ludocode.ludocodebackend.progress.app.service
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForProgress
import com.ludocode.ludocodebackend.progress.api.dto.internal.CourseProgressWithCompletion
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressStats
import com.ludocode.ludocodebackend.progress.app.mapper.CourseProgressMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID

@Service
class CourseProgressService(
    private val courseProgressRepository: CourseProgressRepository,
    private val courseProgressMapper: CourseProgressMapper,
    private val catalogPortForProgress: CatalogPortForProgress,
    private val clock: Clock,
    private val lessonCompletionRepository: LessonCompletionRepository,
) : CourseProgressPortForUser {

    @Transactional
     override fun findOrCreate(userId: UUID, courseId: UUID) : CourseProgressResponseWithEnrolled {
        val firstLessonOfCourse = catalogPortForProgress.findFirstLessonIdInCourse(courseId)
        courseProgressRepository.upsert(userId, courseId, firstLessonOfCourse!!, OffsetDateTime.now(clock))
        val userCourseProgress = courseProgressRepository.findProgressWithModule(userId, courseId)
        val enrolled = courseProgressRepository.findAllCourseIdsForUser(userId)
        return courseProgressMapper.toCourseProgressResponseWithEnrolled(userCourseProgress!!, enrolled)
    }


    override fun existsAnyByUserId (userId: UUID) : Boolean {
        return courseProgressRepository.existsByUser(userId)
    }


    @Transactional
    internal fun resetUserCourseProgress(userId: UUID, courseId: UUID) : CourseProgressResponse {
        lessonCompletionRepository.deleteLessonCompletionsForUserAndCourse(userId, courseId)
        val firstLessonIdInCourse = catalogPortForProgress.findFirstLessonIdInCourse(courseId)
        courseProgressRepository.resetCourseProgressForUser(userId, courseId, firstLessonIdInCourse)
        return findCourseProgress(userId, courseId)
    }


    internal fun updateLesson(userId: UUID, courseId: UUID, isCompleted: Boolean, newLessonId: UUID?, currentLessonId: UUID) : CourseProgressWithCompletion? {

        val courseProgress = courseProgressRepository.findById(CourseProgressId(userId, courseId)).orElseThrow()
        val hasJustFinishedCurrentLesson = courseProgress.currentLessonId == currentLessonId
        if (!hasJustFinishedCurrentLesson && isCompleted) return CourseProgressWithCompletion(findCourseProgress(userId, courseId), false)

        var isFirstCompletion = false

        if (newLessonId != null) {
            courseProgress.currentLessonId = newLessonId
            courseProgress.updatedAt = OffsetDateTime.now(clock)
            courseProgressRepository.save(courseProgress)
        } else {
            if (!courseProgress.isComplete) {
                courseProgressRepository.markCourseComplete(userId, courseId)
                isFirstCompletion = true
            }
        }
        return CourseProgressWithCompletion(findCourseProgress(userId, courseId), isFirstCompletion)
    }

    internal fun getCourseProgressStats(
        userId: UUID,
        courseIds: List<UUID>
    ): List<CourseProgressStats> {

        return courseProgressRepository
            .findCourseLessonStats(userId, courseIds)
            .map { row ->
                CourseProgressStats(
                    courseId = row.courseId,
                    totalLessons = row.totalLessons.toInt(),
                    completedLessons = row.completedLessons.toInt()
                )
            }
    }

    internal fun getEnrolledCourseIds(userId: UUID) : List<UUID> {
        return courseProgressRepository.findAllCourseIdsForUser(userId)
    }

   internal fun findCurrentCourseId(userId: UUID) : UUID? {
        return courseProgressRepository.findCurrentCourseIdForUser(userId)
    }

    internal fun findCourseProgressList(courseIds: List<UUID>, userId: UUID) : List<CourseProgressResponse> {
        return courseProgressMapper.toCourseProgressResponseList(courseProgressRepository.findAllProgressWithModulesByUserAndCourses(userId, courseIds))
    }




    private fun findCourseProgress(userId: UUID, courseId: UUID): CourseProgressResponse {
        return courseProgressMapper.toCourseProgressResponse(courseProgressRepository.findProgressWithModule(userId, courseId) ?: throw IllegalStateException("progress not found"))
    }

}