package com.ludocode.ludocodebackend.catalog.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.tag.api.dto.TagMetadata
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class TagsIT : AbstractIntegrationTest() {

    @BeforeEach
    fun seed() {

    }

    @Test
    fun getAllTags_returnsList() {
        val existingTags = listOf(pythonTag, swiftTag)
        val res = submitGetAllTags()
        assertThat(res.map { it.id })
            .containsExactlyInAnyOrderElementsOf(
                existingTags.map { it.id }
            )
    }

    @Test
    fun updateTag_updatesTag_returnsUpdatedList() {
        val tagToChange = swiftTag

        val req = TagMetadata(
            name = "DSA",
            slug = "dsa",
            id = 0
        )

        val res = submitPutTag(tagToChange.id, req)

        val updated = res.firstOrNull { it.id == tagToChange.id }
            ?: org.junit.jupiter.api.fail("Updated tag not found in response")

        assertThat(updated.name).isEqualTo(req.name)
        assertThat(updated.slug).isEqualTo(req.slug)
    }

    @Test
    fun createTag_usesExistingSlug_throwsError() {
        val req = TagMetadata(
            name = "Some new tag",
            slug = pythonTag.slug,
            id = 0
        )

        assertPostTagError(req, ErrorCode.SLUG_EXISTS)
    }

    @Test
    fun updateTag_usesExistingSlug_throwsError() {
        val tagToChange = pythonTag

        val req = TagMetadata(
            name = tagToChange.name,
            slug = swiftTag.slug,
            id = 0
        )

        assertPutTagError(tagToChange.id, req, ErrorCode.SLUG_EXISTS)
    }

    @Test
    fun createTag_createsTag_returnsUpdatedList() {
        val existingTags = listOf(pythonTag, swiftTag)
        val newTagReq = TagMetadata(
            name = "DSA",
            slug = "dsa",
            id = 0
        )

        val res = submitPostTag(newTagReq)
        assertThat(res.map { it.slug })
            .containsExactlyInAnyOrderElementsOf(
                existingTags.map { it.slug } + newTagReq.slug
            )
    }

    @Test
    fun deleteTag_deletesTag_returnsListExcludingDeleted() {
        val newTagReq = TagMetadata(
            id = 0,
            name = "DSA",
            slug = "dsa",
        )

        val existingTags = submitPostTag(newTagReq)

        val tagToDeleteId = existingTags.firstOrNull { it.slug == newTagReq.slug }
            ?.id
            ?: org.junit.jupiter.api.fail("Created tag not found in response")

        val res = submitDeleteTag(tagToDeleteId)
        assertThat(res.size).isEqualTo(existingTags.size - 1)
        assertThat(res.map { it.id })
            .doesNotContain(tagToDeleteId)
    }

    private fun submitGetAllTags(): List<TagMetadata> =
        TestRestClient.getOk(
            ApiPaths.TAGS.BASE,
            userId = user1.id!!,
            Array<TagMetadata>::class.java
        ).toList()

    private fun submitPostTag(req: TagMetadata): List<TagMetadata> =
        TestRestClient.postOk(
            ApiPaths.TAGS.ADMIN_BASE,
            user1.id!!,
            req,
            Array<TagMetadata>::class.java
        ).toList()

    private fun submitPutTag(tagId: Long, req: TagMetadata): List<TagMetadata> =
        TestRestClient.putOk(
            ApiPaths.TAGS.byTagAdmin(tagId),
            user1.id!!,
            req,
            Array<TagMetadata>::class.java
        ).toList()

    private fun submitDeleteTag(tagId: Long): List<TagMetadata> =
        TestRestClient.deleteOk(
            ApiPaths.TAGS.byTagAdmin(tagId),
            user1.id!!,
            Array<TagMetadata>::class.java
        ).toList()

    private fun assertPostTagError(req: TagMetadata, errorCode: ErrorCode): ValidatableResponse =
        TestRestClient.assertError("POST", ApiPaths.TAGS.ADMIN_BASE, user1.id!!, req, errorCode)

    private fun assertPutTagError(tagId: Long, req: TagMetadata, errorCode: ErrorCode): ValidatableResponse =
        TestRestClient.assertError("PUT", ApiPaths.TAGS.byTagAdmin(tagId), user1.id!!, req, errorCode)

    private fun assertDeleteTagError(tagId: Long, errorCode: ErrorCode): ValidatableResponse =
        TestRestClient.assertError("DELETE", ApiPaths.TAGS.byTagAdmin(tagId), user1.id!!, null, errorCode)

}