package com.ludocode.ludocodebackend.user.integration

import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class DeleteUserIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }


    @Test
    fun deleteUser_thenLoginAgain_createsNewUser() {

        val originalUser = user1
        val originalUserId = originalUser.id!!

        assert(externalAccountRepository.findByUserId(originalUserId) != null)

        val originalExternalAccount = externalAccountRepository.findByUserId(originalUserId)!!

        val originalGoogleSub = originalExternalAccount.providerUserId

        TestRestClient.deleteNoContent(
            "${ApiPaths.USERS.BASE}${ApiPaths.USERS.ME}",
            originalUserId,
        )

        val users = TestRestClient.getOk(
            ApiPaths.USERS.fromIds(listOf(originalUserId)),
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
                .post("${ApiPaths.AUTH.BASE}${ApiPaths.AUTH.FIREBASE}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(UserLoginResponse::class.java)

        val newUserId = loginResponse.user.id

        assert(newUserId != originalUserId)

        val newUser = userRepository.findById(newUserId).orElseThrow()
        assert(!newUser.isDeleted)

        val newUsers = TestRestClient.getOk(
            ApiPaths.USERS.fromIds(listOf(newUserId)),
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
            ApiPaths.USERS.fromIds(listOf(userToDelete.id)),
            userToDelete.id!!,
            Array<UserResponse>::class.java
        )

        assert(users.isEmpty())

    }

    private fun submitPostDeleteUser(userId: UUID) =
        TestRestClient.deleteNoContent("${ApiPaths.USERS.BASE}${ApiPaths.USERS.ME}", userId)


}