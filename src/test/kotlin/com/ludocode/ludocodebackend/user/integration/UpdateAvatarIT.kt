package com.ludocode.ludocodebackend.user.integration

import com.ludocode.ludocodebackend.commons.constants.PathConstants.CHANGE_AVATAR
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS_FROM_IDS
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.response.AvatarInfo
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class UpdateAvatarIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }

    @Test
    fun getUser_returnsAvatar() {
        val user = user1;
        val res = submitGetUser(user.id)
        assertThat(res.avatarVersion).isEqualTo("v1")
        assertThat(res.avatarIndex).isEqualTo(1)
    }

    @Test
    fun updateUserAvatar_returnsUpdatedAvatar() {
        val user = user1
        val req = AvatarInfo("v1", 4)
        val res = submitPostUpdateAvatar(user.id, req)
        assertThat(res).isNotNull()
        assertThat(res.avatarVersion).isEqualTo(req.version)
        assertThat(res.avatarIndex).isEqualTo(req.index)
        assertThat(res.id).isEqualTo(user.id)
    }

    @Test
    fun updateUserAvatar_outOfBoundsIndex_abortsAndReturnsError () {
        val user = user1
        val req = AvatarInfo("v1", 30)
        submitPostErrorUpdateAvatar(user.id, req, ErrorCode.BAD_REQ)

        val res = submitGetUser(userId = user.id)
        assertThat(res.avatarIndex).isEqualTo(user.avatarIndex)
        assertThat(res.avatarVersion).isEqualTo(user.avatarVersion)
    }

    private fun submitGetUser(userId: UUID) : UserResponse {
        val users = TestRestClient.getOk("$USERS$USERS_FROM_IDS?userIds=${userId}", userId, Array<UserResponse>::class.java)
        assertThat(users).isNotEmpty()
        assertThat(users.size).isEqualTo(1)
        return users[0]
    }

    private fun submitPostUpdateAvatar(userId: UUID, req: AvatarInfo) : UserResponse {
        return TestRestClient.postOk("$USERS$CHANGE_AVATAR", userId, req, UserResponse::class.java)
    }

    private fun submitPostErrorUpdateAvatar(userId: UUID, req: AvatarInfo, statusCode: ErrorCode) : ValidatableResponse? {
        return TestRestClient.assertError("POST", "$USERS$CHANGE_AVATAR", userId, req, statusCode)
    }


}