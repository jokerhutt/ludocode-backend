package com.ludocode.ludocodebackend.preferences.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.preferences.api.dto.response.CareerResponse
import com.ludocode.ludocodebackend.preferences.api.dto.request.PreferenceRequestKey

import com.ludocode.ludocodebackend.preferences.api.dto.request.TogglePreferencesRequest
import com.ludocode.ludocodebackend.preferences.api.infra.repository.CareerPreferencesRepository
import com.ludocode.ludocodebackend.preferences.api.infra.repository.UserPreferencesRepository
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.preferences.api.dto.request.OnboardingSubmission
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

    fun getCareerPreferences(): List<CareerResponse> {
        return careerPreferencesRepository.findAll().map {
            CareerResponse(
                id = it.id,
                title = it.title,
                description = it.description,
                defaultCourseId = it.courseId,
                choice = it.choice
            )
        }
    }

    @Transactional
    internal fun updatePreference(
        userId: UUID,
        req: TogglePreferencesRequest
    ): UserPreferences {

        val prefs = getPreferences(userId)

        when (req.key) {
            PreferenceRequestKey.AI -> prefs.aiEnabled = req.value
            PreferenceRequestKey.AUDIO -> prefs.audioEnabled = req.value
        }

        return prefs
    }


    internal fun getPreferences(userId: UUID): UserPreferences {
        val preferences = userPreferencesRepository.findById(userId).orElseThrow()
        return preferences
    }


}