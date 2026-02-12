package com.ludocode.ludocodebackend.progress.integration
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.preferences.api.dto.TogglePreferencesRequest
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import com.ludocode.ludocodebackend.user.domain.enums.DesiredPath
import org.assertj.core.api.Assertions.assertThat
import java.util.UUID
import kotlin.test.Test

class OnboardingIT : AbstractIntegrationTest() {

    @Test
    fun submitOnboarding_returnsCourseAndPreferences () {

        val user = user1
        val submission = OnboardingSubmission(
            chosenPath = DesiredPath.DATA,
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

        assertThat(res.preferences.chosenPath).isEqualTo(DesiredPath.DATA)

    }

    @Test
    fun getInitialPreferences_bothEnabledByDefault () {

        val userId = user1.id

        initializePreferences(userId)

        val res = submitGetForPreferences(user1.id)
        assertThat(res).isNotNull()
        assertThat(res.aiEnabled).isTrue()
        assertThat(res.audioEnabled).isTrue()

    }

    @Test
    fun submitPreferences_updatesPreferences () {

        val userId = user1.id

        initializePreferences(userId)

        val audioEnabled = false
        val aiEnabled = false
        val req = TogglePreferencesRequest(audioEnabled, aiEnabled)

        val res = submitPatchForPreferences(userId, req)
        assertThat(res).isNotNull()
        assertThat(res.aiEnabled).isFalse()
        assertThat(res.audioEnabled).isFalse()

    }

    @Test
    fun submitPreferences_updatesOnePreferences_updatesOnlyOne() {
        val userId = user1.id

        initializePreferences(userId)

        val audioEnabled = true
        val aiEnabled = false

        val req = TogglePreferencesRequest(audioEnabled, aiEnabled)

        val res = submitPatchForPreferences(userId, req)
        assertThat(res).isNotNull()
        assertThat(res.aiEnabled).isFalse()
        assertThat(res.audioEnabled).isTrue()

    }

    private fun initializePreferences (userId: UUID) {
        val submission = OnboardingSubmission(
            chosenPath = DesiredPath.DATA,
            chosenCourse = pythonId,
            hasProgrammingExperience = false,
            selectedUsername = "John Doe"
        )

        submitPostForOnboarding(userId, submission)
    }


    private fun submitPostForOnboarding(userId: UUID, submission: OnboardingSubmission): OnboardingResponse =
        TestRestClient.putOk("${ApiPaths.PREFERENCES.BASE}", userId, submission, OnboardingResponse::class.java)

    private fun submitPatchForPreferences(userId: UUID, submission: TogglePreferencesRequest) : UserPreferences =
        TestRestClient.patchOk("${ApiPaths.PREFERENCES.BASE}", userId, submission, UserPreferences::class.java)

    private fun submitGetForPreferences(userId: UUID) : UserPreferences =
        TestRestClient.getOk("${ApiPaths.PREFERENCES.BASE}", userId, UserPreferences::class.java)

}