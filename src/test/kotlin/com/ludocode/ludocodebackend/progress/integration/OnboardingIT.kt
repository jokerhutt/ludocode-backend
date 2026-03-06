package com.ludocode.ludocodebackend.progress.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.preferences.api.dto.PreferenceRequestKey
import com.ludocode.ludocodebackend.preferences.api.dto.TogglePreferencesRequest
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.preferences.domain.entity.UserPreferences
import org.assertj.core.api.Assertions.assertThat
import java.util.*
import kotlin.test.Test

class OnboardingIT : AbstractIntegrationTest() {

    @Test
    fun submitOnboarding_returnsCourseAndPreferences() {

        user1
        val submission = OnboardingSubmission(
            chosenPath = "DATA",
            chosenCourse = pythonId,
            hasProgrammingExperience = false,
            selectedUsername = "John Doe"
        )

        val res = submitPostForOnboarding(user1.id!!, submission)

        assertThat(res).isNotNull()

        assertThat(res.courseProgressResponse.courseProgress.courseId).isEqualTo(pythonId)
        assertThat(res.courseProgressResponse.enrolled.size).isEqualTo(1)
        assertThat(res.courseProgressResponse.courseProgress.moduleId).isEqualTo(pyMod1Id)
        assertThat(res.refreshedUser.displayName).isEqualTo(submission.selectedUsername)

        assertThat(res.preferences.chosenPathId).isEqualTo(dataPath.id)

    }

    @Test
    fun getInitialPreferences_bothEnabledByDefault() {

        val userId = user1.id

        initializePreferences(userId)

        val res = submitGetForPreferences(user1.id)
        assertThat(res).isNotNull()
        assertThat(res.aiEnabled).isTrue()
        assertThat(res.audioEnabled).isTrue()

    }

    @Test
    fun submitPreferences_EnablesAI() {
        val userId = user1.id
        initializePreferences(userId)
        val req = TogglePreferencesRequest(true, PreferenceRequestKey.AUDIO)
        val res = submitPatchForPreferences(userId, req)
        assertThat(res).isNotNull()
        assertThat(res.aiEnabled).isTrue()
    }

    @Test
    fun submitPreferences_DisablesAI() {
        val userId = user1.id
        initializePreferences(userId)
        val req = TogglePreferencesRequest(false, PreferenceRequestKey.AI)
        val res = submitPatchForPreferences(userId, req)
        assertThat(res).isNotNull()
        assertThat(res.aiEnabled).isFalse()
    }

    @Test
    fun submitPreferences_EnablesAudio() {
        val userId = user1.id
        initializePreferences(userId)
        val req = TogglePreferencesRequest(true, PreferenceRequestKey.AUDIO)
        val res = submitPatchForPreferences(userId, req)
        assertThat(res).isNotNull()
        assertThat(res.audioEnabled).isTrue()
    }

    @Test
    fun submitPreferences_DisablesAudio() {
        val userId = user1.id
        initializePreferences(userId)
        val req = TogglePreferencesRequest(false, PreferenceRequestKey.AUDIO)
        val res = submitPatchForPreferences(userId, req)
        assertThat(res).isNotNull()
        assertThat(res.audioEnabled).isFalse()
    }

    private fun initializePreferences(userId: UUID) {
        val submission = OnboardingSubmission(
            chosenPath = "DATA",
            chosenCourse = pythonId,
            hasProgrammingExperience = false,
            selectedUsername = "John Doe"
        )

        submitPostForOnboarding(userId, submission)
    }


    private fun submitPostForOnboarding(userId: UUID, submission: OnboardingSubmission): OnboardingResponse =
        TestRestClient.putOk("${ApiPaths.PREFERENCES.BASE}", userId, submission, OnboardingResponse::class.java)

    private fun submitPatchForPreferences(userId: UUID, submission: TogglePreferencesRequest): UserPreferences =
        TestRestClient.patchOk("${ApiPaths.PREFERENCES.BASE}", userId, submission, UserPreferences::class.java)

    private fun submitGetForPreferences(userId: UUID): UserPreferences =
        TestRestClient.getOk("${ApiPaths.PREFERENCES.BASE}", userId, UserPreferences::class.java)

}