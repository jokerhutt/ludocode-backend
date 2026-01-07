package com.ludocode.ludocodebackend.user.integration
import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.commons.constants.PathConstants.AUTH
import com.ludocode.ludocodebackend.commons.constants.PathConstants.DELETE_USER
import com.ludocode.ludocodebackend.commons.constants.PathConstants.FIREBASE_LOGIN
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS
import com.ludocode.ludocodebackend.commons.constants.PathConstants.USERS_FROM_IDS
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DeleteUserIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed () {

    }


        @Test
        fun deleteUser_thenLoginAgain_createsNewUser() {

            val originalUser = user1
            val originalUserId = originalUser.id!!

            assert(externalAccountRepository.findByUserId(originalUserId) != null)

            val originalExternalAccount = externalAccountRepository.findByUserId(originalUserId)!!

            val originalGoogleSub = originalExternalAccount.providerUserId

            TestRestClient.postNoContent(
                "$USERS$DELETE_USER",
                originalUserId,
                null
            )

            val users = TestRestClient.getOk(
                "$USERS$USERS_FROM_IDS?userIds=${originalUserId}",
                originalUserId,
                Array<UserResponse>::class.java
            )

            assert(users.isEmpty())

            assert(externalAccountRepository.findByUserId(originalUserId) == null)

            val loginResponse =
                given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer fake-firebase-token")
                    .`when`()
                    .post("$AUTH$FIREBASE_LOGIN")
                    .then()
                    .statusCode(200)
                    .extract()
                    .`as`(UserLoginResponse::class.java)

            val newUserId = loginResponse.user.id

            assert(newUserId != originalUserId)

            val newUser = userRepository.findById(newUserId).orElseThrow()
            assert(!newUser.isDeleted)

            val newUsers = TestRestClient.getOk(
                "$USERS$USERS_FROM_IDS?userIds=${newUserId}",
                newUserId,
                Array<UserResponse>::class.java
            )
            assertThat(newUsers.size).isEqualTo(1)
            assertThat(newUsers[0].id == newUserId)

            val newExternalAccount =
                externalAccountRepository.findByUserId(newUserId)!!

            assertThat(newExternalAccount.provider).isEqualTo(AuthProvider.FIREBASE)
            assertThat(newExternalAccount.providerUserId).isEqualTo(originalGoogleSub)


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