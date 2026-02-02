package com.ludocode.ludocodebackend.onboarding.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.onboarding.api.dto.TogglePreferencesRequest
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForAuth
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForOnboarding
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import com.ludocode.ludocodebackend.onboarding.api.infra.repository.UserPreferencesRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PreferencesService(
    private val userPortForOnboarding: UserPortForOnboarding,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val courseProgressPortForUser: CourseProgressPortForUser,
    private val userPortForAuth: UserPortForAuth
) {

    private val logger = LoggerFactory.getLogger(PreferencesService::class.java)

    @Transactional
    internal fun createPreferences (submission: OnboardingSubmission, userId: UUID) : OnboardingResponse {
        val toSubmit = UserPreferences(userId = userId, hasExperience = submission.hasProgrammingExperience, chosenPath = submission.chosenPath)

        val selectedUser = userPortForOnboarding.setDisplayName(userId, submission.selectedUsername)

        val savedPreferences = userPreferencesRepository.save(toSubmit)
        val newCourseProgressWithEnrolled = courseProgressPortForUser.findOrCreate(userId, submission.chosenCourse)

        logger.info(
            LogEvents.USER_ONBOARDED + " {} {}",
            kv(LogFields.CHOSEN_PATH, submission.chosenPath.name),
            kv(LogFields.COURSE_ID, submission.chosenCourse.toString())
        )

        return OnboardingResponse(refreshedUser = userPortForAuth.getById(userId), savedPreferences, courseProgressResponse = newCourseProgressWithEnrolled)
    }

    @Transactional
    internal fun updateTogglePreferences (userId: UUID, req: TogglePreferencesRequest) : UserPreferences {
        val currentPreferences = getPreferences(userId)

        currentPreferences.audioEnabled = req.audioEnabled
        currentPreferences.aiEnabled = req.aiEnabled

        return currentPreferences
    }

    internal fun getPreferences (userId: UUID) : UserPreferences {
        val preferences = userPreferencesRepository.findById(userId).orElseThrow()
        return preferences
    }


}