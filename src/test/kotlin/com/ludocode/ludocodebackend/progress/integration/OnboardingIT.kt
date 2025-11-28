package com.ludocode.ludocodebackend.progress.integration
import com.ludocode.ludocodebackend.commons.constants.PathConstants.SUBMIT_ONBOARDING
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
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
            hasProgrammingExperience = false
        )

        val res = submitPostForOnboarding(user1.id!!, submission)

        assertThat(res).isNotNull()

        assertThat(res.courseProgressResponse.courseProgress.courseId).isEqualTo(pythonId)
        assertThat(res.courseProgressResponse.enrolled.size).isEqualTo(1)
        assertThat(res.courseProgressResponse.courseProgress.currentLessonId).isEqualTo(py1L1)

        assertThat(res.preferences.chosenPath).isEqualTo(DesiredPath.DATA)

    }

    private fun submitPostForOnboarding(userId: UUID, submission: OnboardingSubmission): OnboardingResponse =
        TestRestClient.postOk("$USERS$SUBMIT_ONBOARDING", userId, submission, OnboardingResponse::class.java)
}