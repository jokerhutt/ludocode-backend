package com.ludocode.ludocodebackend.preferences.app.service

import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.preferences.api.dto.CareerOption
import com.ludocode.ludocodebackend.preferences.api.dto.CourseOption
import com.ludocode.ludocodebackend.preferences.api.dto.ExperienceOption
import com.ludocode.ludocodebackend.preferences.api.dto.OnboardingDraftResponse
import com.ludocode.ludocodebackend.preferences.api.dto.OnboardingFormResponse
import com.ludocode.ludocodebackend.preferences.api.dto.TogglePreferencesRequest
import com.ludocode.ludocodebackend.preferences.api.infra.repository.CareerPreferencesRepository
import com.ludocode.ludocodebackend.preferences.api.infra.repository.UserPreferencesRepository
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForAuth
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForOnboarding
import com.ludocode.ludocodebackend.preferences.domain.entity.UserPreferences
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class PreferencesService(
    private val userPortForOnboarding: UserPortForOnboarding,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val courseProgressPortForUser: CourseProgressPortForUser,
    private val userPortForAuth: UserPortForAuth,
    private val careerPreferencesRepository: CareerPreferencesRepository,
    private val courseRepository: CourseRepository
) {

    private val logger = LoggerFactory.getLogger(PreferencesService::class.java)

    @Transactional
    internal fun createPreferences(submission: OnboardingSubmission, userId: UUID): OnboardingResponse {

        val chosenCareerPreference = careerPreferencesRepository.findByChoice(submission.chosenPath)
            ?: throw ApiException(ErrorCode.CAREER_PREFERENCE_NOT_FOUND)


        val toSubmit = UserPreferences(
            userId = userId,
            hasExperience = submission.hasProgrammingExperience,
            chosenPathId = chosenCareerPreference.id,
            chosenCourseId = submission.chosenCourse
        )

        userPortForOnboarding.setDisplayName(userId, submission.selectedUsername)

        val savedPreferences = userPreferencesRepository.save(toSubmit)
        val newCourseProgressWithEnrolled = courseProgressPortForUser.findOrCreate(userId, submission.chosenCourse)

        logger.info(
            LogEvents.USER_ONBOARDED + " {} {}",
            kv(LogFields.CHOSEN_PATH, submission.chosenPath),
            kv(LogFields.COURSE_ID, submission.chosenCourse.toString())
        )

        return OnboardingResponse(
            refreshedUser = userPortForAuth.getById(userId),
            savedPreferences,
            courseProgressResponse = newCourseProgressWithEnrolled
        )
    }

    fun getOnboardingForm(): OnboardingFormResponse {
        val careers = careerPreferencesRepository.findAll().map {
            CareerOption(
                id = it.id!!,
                title = it.title,
                description = it.description,
                defaultCourseId = it.courseId
            )
        }

        val courses = courseRepository.findAll().map {
            CourseOption(
                courseId = it.id,
                title = it.title,
                description = it.description
            )
        }

        return OnboardingFormResponse(
            courses = courses,
            careers = careers,
            experienceOptions = listOf(
                ExperienceOption(false, "No, I've never programmed before"),
                ExperienceOption(true, "Yes, I have programmed before")
            )
        )
    }

    @Transactional
    internal fun updateTogglePreferences(userId: UUID, req: TogglePreferencesRequest): UserPreferences {
        val currentPreferences = getPreferences(userId)

        currentPreferences.audioEnabled = req.audioEnabled
        currentPreferences.aiEnabled = req.aiEnabled

        return currentPreferences
    }

    internal fun getPreferences(userId: UUID): UserPreferences {
        val preferences = userPreferencesRepository.findById(userId).orElseThrow()
        return preferences
    }


}