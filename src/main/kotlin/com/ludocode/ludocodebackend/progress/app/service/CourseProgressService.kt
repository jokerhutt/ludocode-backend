package com.ludocode.ludocodebackend.progress.app.service
import com.ludocode.ludocodebackend.catalog.app.port.`in`.CatalogPortForProgress
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.progress.api.dto.internal.CourseProgressWithCompletion
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponse
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressResponseWithEnrolled
import com.ludocode.ludocodebackend.progress.api.dto.response.CourseProgressStats
import com.ludocode.ludocodebackend.progress.app.mapper.CourseProgressMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import com.ludocode.ludocodebackend.progress.infra.repository.CourseProgressRepository
import com.ludocode.ludocodebackend.progress.infra.repository.LessonCompletionRepository
import jakarta.persistence.EntityManager
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
        val firstModuleOfCourse = catalogPortForProgress.findFirstModuleIdInCourse(courseId)
        courseProgressRepository.upsert(userId, courseId, firstModuleOfCourse, OffsetDateTime.now(clock))
        val userCourseProgress = courseProgressRepository.findById(CourseProgressId(userId, courseId)).orElseThrow { ApiException(
            ErrorCode.COURSE_PROGRESS_NOT_FOUND) }
        val enrolled = courseProgressRepository.findAllCourseIdsForUser(userId)
        return courseProgressMapper.toCourseProgressResponseWithEnrolled(userCourseProgress, enrolled)
    }

    override fun existsAnyByUserId (userId: UUID) : Boolean {
        return courseProgressRepository.existsByUser(userId)
    }

    @Transactional
    internal fun resetUserCourseProgress(userId: UUID, courseId: UUID) : CourseProgressResponse {
        lessonCompletionRepository.deleteLessonCompletionsForUserAndCourse(userId, courseId)
        val firstModuleIdInCourse = catalogPortForProgress.findFirstModuleIdInCourse(courseId)
        val courseProgress = courseProgressRepository.findById(CourseProgressId(userId, courseId)).orElseThrow { ApiException(ErrorCode.COURSE_PROGRESS_NOT_FOUND)  }
        courseProgress.currentModuleId = firstModuleIdInCourse
        courseProgress.isComplete = false
        return findCourseProgress(userId, courseId)
    }

    internal fun updateLesson(userId: UUID, courseId: UUID, currentLessonId: UUID) : CourseProgressWithCompletion? {

        val currentLessonModule = catalogPortForProgress.findModuleIdForLesson(currentLessonId)
        val courseProgress = courseProgressRepository.findById(CourseProgressId(userId, courseId)).orElseThrow()
        courseProgress.currentModuleId = currentLessonModule

        val courseProgressStats = courseProgressRepository.findSingleCourseStats(userId, courseId) ?: throw ApiException(
            ErrorCode.COURSE_STATS_NOT_FOUND)

        println("TOTAL: " + courseProgressStats.totalLessons + " COMPLETED: " + courseProgressStats.completedLessons)
        val isCourseComplete = courseProgressStats.completedLessons == courseProgressStats.totalLessons
        var isCourseCompleteForFirstTime = false

        if (isCourseComplete && !courseProgress.isComplete) {
            courseProgress.isComplete = true
            isCourseCompleteForFirstTime = true
        }

        return CourseProgressWithCompletion(findCourseProgress(userId, courseId), isCourseCompleteForFirstTime)
    }

    internal fun getCourseProgressStats(
        userId: UUID,
        courseIds: List<UUID>
    ): List<CourseProgressStats> {

        return courseProgressRepository
            .findCourseLessonStatsList(userId, courseIds)
            .map { row ->
                CourseProgressStats(
                    id = row.courseId,
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
        return courseProgressMapper.toCourseProgressResponseList(courseProgressRepository.findByIdUserIdAndIdCourseIdIn(userId, courseIds))
    }

    private fun findCourseProgress(userId: UUID, courseId: UUID): CourseProgressResponse {
        return courseProgressMapper.toCourseProgressResponse(courseProgressRepository.findById(CourseProgressId(userId, courseId)).orElseThrow { ApiException(
            ErrorCode.COURSE_PROGRESS_NOT_FOUND) })
    }

}