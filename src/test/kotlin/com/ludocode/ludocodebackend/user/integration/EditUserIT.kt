package com.ludocode.ludocodebackend.user.integration
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.response.AvatarInfo
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class EditUserIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }

    @Test
    fun editUserProfile_returnsEditedProfile() {
        val user = user1
        val req = AvatarInfo("v1", 4)
        val res = submitPutEditUser(user.id, req)
        assertThat(res).isNotNull()
        assertThat(res.avatarVersion).isEqualTo(req.version)
        assertThat(res.avatarIndex).isEqualTo(req.index)
        assertThat(res.id).isEqualTo(user.id)
    }

    @Test
    fun editUserProfile_outOfBoundsAvatarIndex_abortsAndReturnsError() {
        val user = user1
        val req = AvatarInfo("v1", 30)
        submitPutErrorEditProfile(user.id, req, ErrorCode.BAD_REQ)

        val res = submitGetUser(userId = user.id)
        assertThat(res.avatarIndex).isEqualTo(user.avatarIndex)
        assertThat(res.avatarVersion).isEqualTo(user.avatarVersion)
    }

    private fun submitGetUser(userId: UUID): UserResponse {
        val users =
            TestRestClient.getOk(ApiPaths.USERS.fromIds(listOf(userId)), userId, Array<UserResponse>::class.java)
        assertThat(users).isNotEmpty()
        assertThat(users.size).isEqualTo(1)
        return users[0]
    }

    private fun submitPutEditUser(userId: UUID, req: AvatarInfo): UserResponse {
        return TestRestClient.putOk("${ApiPaths.USERS.BASE}${ApiPaths.USERS.AVATAR}", userId, req, UserResponse::class.java)
    }

    private fun submitPutErrorEditProfile(
        userId: UUID,
        req: AvatarInfo,
        statusCode: ErrorCode
    ): ValidatableResponse? {
        return TestRestClient.assertError("PUT", "${ApiPaths.USERS.BASE}${ApiPaths.USERS.AVATAR}", userId, req, statusCode)
    }

}