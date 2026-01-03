package com.ludocode.ludocodebackend.user.integration
import com.ludocode.ludocodebackend.commons.constants.PathConstants.DELETE_USER
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS_FROM_IDS
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DeleteUserIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }

    @Test
    fun deleteUser_deletesUser() {

        val userToDelete = user1
        submitPostDeleteUser(userToDelete.id!!)

        val users = TestRestClient.getOk(
            "$USERS$USERS_FROM_IDS?userIds=${userToDelete.id}",
            userToDelete.id!!,
            Array<UserResponse>::class.java
        )

        assert(users.isEmpty())

    }

    private fun submitPostDeleteUser(userId: UUID) =
        TestRestClient.postNoContent("$USERS$DELETE_USER", userId, null)


}