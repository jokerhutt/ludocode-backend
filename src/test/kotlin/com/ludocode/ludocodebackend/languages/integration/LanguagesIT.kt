package com.ludocode.ludocodebackend.languages.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.api.dto.CreateLanguageRequest
import com.ludocode.ludocodebackend.languages.api.dto.LanguageDisabledMessageRequest
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.languages.api.dto.LanguageToggleRequest
import com.ludocode.ludocodebackend.languages.api.dto.UpdateLanguageRequest
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import kotlin.test.Test

class LanguagesIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }

    @Test
    fun getAllLanguages_returnsList() {
        val existingLanguages = listOf(
            pythonLanguage, luaLanguage, swiftLanguage, jsLanguage
        )
        val res = submitGetALlLanguages()
        assertThat(res.map { it.languageId })
            .containsExactlyInAnyOrderElementsOf(
                existingLanguages.map { it.id }
            )
    }

    @Test
    fun updateLanguage_usesExistingSlug_throwsError() {
        listOf(
            pythonLanguage, luaLanguage, swiftLanguage, jsLanguage
        )
        val languageToChange = pythonLanguage
        val req = UpdateLanguageRequest(
            name = pythonLanguage.name,
            editorId = pythonLanguage.editorId,
            pistonId = pythonLanguage.pistonId,
            slug = jsLanguage.slug,
            extension = pythonLanguage.extension,
            base = "main",
            iconName = "Python Icon",
            initialScript = "print('I have been updated!')"
        )

        assertPutLanguageError(languageToChange.id, req, ErrorCode.SLUG_EXISTS)
    }

    @Test
    fun updateLanguage_usesExistingEditorId_throwsError() {
        listOf(
            pythonLanguage, luaLanguage, swiftLanguage, jsLanguage
        )
        val languageToChange = pythonLanguage
        val req = UpdateLanguageRequest(
            name = pythonLanguage.name,
            editorId = jsLanguage.editorId,
            pistonId = pythonLanguage.pistonId,
            slug = pythonLanguage.slug,
            extension = pythonLanguage.extension,
            base = "main",
            iconName = "Python Icon",
            initialScript = "print('I have been updated!')"
        )

        assertPutLanguageError(languageToChange.id, req, ErrorCode.EDITOR_ID_EXISTS)
    }

    @Test
    fun updateLanguage_usesExistingPistonId_throwsError() {
        listOf(
            pythonLanguage, luaLanguage, swiftLanguage, jsLanguage
        )
        val languageToChange = pythonLanguage
        val req = UpdateLanguageRequest(
            name = pythonLanguage.name,
            editorId = pythonLanguage.editorId,
            pistonId = luaLanguage.pistonId,
            slug = pythonLanguage.slug,
            extension = pythonLanguage.extension,
            base = "main",
            iconName = "Python Icon",
            initialScript = "print('I have been updated!')"
        )

        assertPutLanguageError(languageToChange.id, req, ErrorCode.PISTON_ID_EXISTS)
    }

    @Test
    fun deleteLanguage_deletesLanguage_returnsListExcludingDeleted() {
        val newLanguage = CreateLanguageRequest(
            name = "SQL",
            extension = ".sql",
            pistonId = "sql",
            editorId = "sql",
            base = "db",
            iconName = "Sql",
            runtimeVersion = "*",
            initialScript = "SELECT * FROM USERS;",
            slug = "sql"
        )

        val existingLanguages = submitPostLanguage(newLanguage)
        val languageToDeleteId = existingLanguages.firstOrNull { it.slug == newLanguage.slug }
            ?.languageId
            ?: fail("Created language not found in response")

        val res = submitDeleteLanguage(languageToDeleteId)
        assertThat(res.size).isEqualTo(existingLanguages.size - 1)
        assertThat(res.map { it.languageId })
            .doesNotContain(languageToDeleteId)
    }

    @Test
    fun deleteLanguage_alreadyDeleted_throwsError() {
        val newLanguage = CreateLanguageRequest(
            name = "SQL",
            extension = ".sql",
            pistonId = "sql",
            editorId = "sql",
            base = "db",
            iconName = "Sql",
            runtimeVersion = "*",
            initialScript = "SELECT * FROM USERS;",
            slug = "sql"
        )

        val existingLanguages = submitPostLanguage(newLanguage)
        val languageToDeleteId = existingLanguages.firstOrNull { it.slug == newLanguage.slug }
            ?.languageId
            ?: fail("Created language not found in response")
        val res = submitDeleteLanguage(languageToDeleteId)
        assertThat(res.size).isEqualTo(existingLanguages.size - 1)
        assertThat(res.map { it.languageId })
            .doesNotContain(languageToDeleteId)

        assertDeleteLanguageError(languageToDeleteId, ErrorCode.LANGUAGE_NOT_FOUND)
    }

    @Test
    fun createLanguage_createsLangugae_returnsUpdatedList() {
        val existingLanguages = listOf(
            pythonLanguage, luaLanguage, swiftLanguage, jsLanguage
        )
        val newLanguage = CreateLanguageRequest(
            name = "SQL",
            extension = ".sql",
            pistonId = "sql",
            editorId = "sql",
            base = "db",
            iconName = "Sql",
            initialScript = "SELECT * FROM USERS;",
            slug = "sql"
        )

        val res = submitPostLanguage(newLanguage)
        assertThat(res.map { it.slug })
            .containsExactlyInAnyOrderElementsOf(
                existingLanguages.map { it.slug } + newLanguage.slug
            )
    }

    @Test
    fun createLanguage_usesExistingSlug_throwsError() {
        listOf(
            pythonLanguage, luaLanguage, swiftLanguage, jsLanguage
        )
        val newLanguage = CreateLanguageRequest(
            name = "SQL",
            extension = ".sql",
            pistonId = "sql",
            editorId = "sql",
            base = "db",
            iconName = "Sql",
            initialScript = "SELECT * FROM USERS;",
            slug = "py"
        )

        assertPostLanguageError(newLanguage, ErrorCode.SLUG_EXISTS)

    }

    @Test
    fun updateLanguage_updatesLanguage_returnsUpdatedList() {
        listOf(
            pythonLanguage, luaLanguage, swiftLanguage, jsLanguage
        )
        pythonLanguage
        val req = UpdateLanguageRequest(
            name = "The all new Python course",
            editorId = pythonLanguage.editorId,
            pistonId = pythonLanguage.pistonId,
            slug = pythonLanguage.slug,
            extension = pythonLanguage.extension,
            base = "main",
            iconName = "Python Icon",
            initialScript = "print('I have been updated!')"
        )
        val res = submitPutLanguage(pythonLanguage.id, req)

        val updated = res.firstOrNull { it.languageId == pythonLanguage.id }
            ?: fail("Updated language not found in response")

        assertThat(updated.base).isEqualTo(req.base)
        assertThat(updated.iconName).isEqualTo(req.iconName)
        assertThat(updated.initialScript).isEqualTo(req.initialScript)

        assertThat(updated.name).isEqualTo(req.name)
        assertThat(updated.slug).isEqualTo(pythonLanguage.slug)
        assertThat(updated.extension).isEqualTo(pythonLanguage.extension)
        assertThat(updated.editorId).isEqualTo(pythonLanguage.editorId)
        assertThat(updated.pistonId).isEqualTo(pythonLanguage.pistonId)

    }

    @Test
    fun setLanguageVisibility_disablesLanguage_andCanBeEnabledBack() {
        val disableReq = LanguageToggleRequest(
            enabled = false,
            message = "Temporarily unavailable"
        )
        val disabledList = submitPutLanguageVisibility(pythonLanguage.id, disableReq)
        val disabledLanguage = disabledList.firstOrNull { it.languageId == pythonLanguage.id }
            ?: fail("Disabled language not found in response")

        assertThat(disabledLanguage.enabled).isFalse()
        assertThat(disabledLanguage.disabledReason).isEqualTo(disableReq.message)

        val enableReq = LanguageToggleRequest(enabled = true)
        val enabledList = submitPutLanguageVisibility(pythonLanguage.id, enableReq)
        val enabledLanguage = enabledList.firstOrNull { it.languageId == pythonLanguage.id }
            ?: fail("Enabled language not found in response")

        assertThat(enabledLanguage.enabled).isTrue()
        assertThat(enabledLanguage.disabledReason).isNull()
    }

    @Test
    fun setDisabledMessage_disabledLanguage_updatesMessage() {
        submitPutLanguageVisibility(
            pythonLanguage.id,
            LanguageToggleRequest(enabled = false, message = "Temporarily unavailable")
        )

        val req = LanguageDisabledMessageRequest(message = "Python is in maintenance mode")
        val res = submitPutLanguageDisabledMessage(pythonLanguage.id, req)
        val updated = res.firstOrNull { it.languageId == pythonLanguage.id }
            ?: fail("Updated language not found in response")

        assertThat(updated.enabled).isFalse()
        assertThat(updated.disabledReason).isEqualTo(req.message)
    }

    @Test
    fun setDisabledMessage_enabledLanguage_throwsBadRequest() {
        val req = LanguageDisabledMessageRequest(message = "Not allowed while enabled")
        assertPutLanguageDisabledMessageError(pythonLanguage.id, req, ErrorCode.BAD_REQ)
    }

    @Test
    fun setLanguageVisibility_disableWithoutMessage_throwsBadRequest() {
        assertPutLanguageVisibilityError(
            pythonLanguage.id,
            LanguageToggleRequest(enabled = false),
            ErrorCode.BAD_REQ
        )
    }


    private fun submitGetALlLanguages(): Array<LanguageMetadata> =
        TestRestClient.getOk(ApiPaths.LANGUAGES.BASE, userId = user1.id, Array<LanguageMetadata>::class.java)

    private fun assertPutLanguageError(
        languageId: Long,
        req: UpdateLanguageRequest,
        statusCode: ErrorCode
    ): ValidatableResponse? {
        return TestRestClient.assertError("PUT", ApiPaths.LANGUAGES.byIdAdmin(languageId), user1.id, req, statusCode)
    }

    private fun assertPostLanguageError(req: CreateLanguageRequest, statusCode: ErrorCode): ValidatableResponse? {
        return TestRestClient.assertError("POST", ApiPaths.LANGUAGES.ADMIN_BASE, user1.id, req, statusCode)
    }

    private fun assertDeleteLanguageError(languageId: Long, statusCode: ErrorCode): ValidatableResponse? {
        return TestRestClient.assertError(
            "DELETE",
            ApiPaths.LANGUAGES.byIdAdmin(languageId),
            user1.id,
            null,
            statusCode
        )
    }

    private fun submitPutLanguage(languageId: Long, req: UpdateLanguageRequest): Array<LanguageMetadata> =
        TestRestClient.putOk(
            ApiPaths.LANGUAGES.byIdAdmin(languageId),
            user1.id,
            req,
            Array<LanguageMetadata>::class.java
        )

    private fun submitPutLanguageVisibility(languageId: Long, req: LanguageToggleRequest): Array<LanguageMetadata> =
        TestRestClient.putOk(
            ApiPaths.LANGUAGES.byIdAdminVisibility(languageId),
            user1.id,
            req,
            Array<LanguageMetadata>::class.java
        )

    private fun submitPutLanguageDisabledMessage(
        languageId: Long,
        req: LanguageDisabledMessageRequest
    ): Array<LanguageMetadata> =
        TestRestClient.putOk(
            ApiPaths.LANGUAGES.byIdAdminDisabledMessage(languageId),
            user1.id,
            req,
            Array<LanguageMetadata>::class.java
        )

    private fun assertPutLanguageDisabledMessageError(
        languageId: Long,
        req: LanguageDisabledMessageRequest,
        statusCode: ErrorCode
    ): ValidatableResponse? {
        return TestRestClient.assertError(
            "PUT",
            ApiPaths.LANGUAGES.byIdAdminDisabledMessage(languageId),
            user1.id,
            req,
            statusCode
        )
    }

    private fun assertPutLanguageVisibilityError(
        languageId: Long,
        req: LanguageToggleRequest,
        statusCode: ErrorCode
    ): ValidatableResponse? {
        return TestRestClient.assertError(
            "PUT",
            ApiPaths.LANGUAGES.byIdAdminVisibility(languageId),
            user1.id,
            req,
            statusCode
        )
    }

    private fun submitPostLanguage(req: CreateLanguageRequest): Array<LanguageMetadata> =
        TestRestClient.postOk(ApiPaths.LANGUAGES.ADMIN_BASE, user1.id, req, Array<LanguageMetadata>::class.java)

    private fun submitDeleteLanguage(languageId: Long): Array<LanguageMetadata> =
        TestRestClient.deleteOk(ApiPaths.LANGUAGES.byIdAdmin(languageId), user1.id, Array<LanguageMetadata>::class.java)

}