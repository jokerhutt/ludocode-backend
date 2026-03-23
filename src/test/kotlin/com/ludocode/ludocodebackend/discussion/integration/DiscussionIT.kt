package com.ludocode.ludocodebackend.discussion.integration

import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.discussion.api.dto.CreateDiscussionMessageRequest
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionMessageResponse
import com.ludocode.ludocodebackend.discussion.api.dto.DiscussionResponse
import com.ludocode.ludocodebackend.discussion.domain.entity.DiscussionMessage
import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import com.ludocode.ludocodebackend.support.snapshot.TestSnapshotService
import io.restassured.response.ValidatableResponse
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.Test

class DiscussionIT : AbstractIntegrationTest() {

    @Autowired
    private lateinit var testSnapshotService: TestSnapshotService

    @Test
    fun noDiscussionYet_returnsEmptyDiscussion() {

        val course = testSnapshotService.buildCourseSnapshot(pythonId)
        val lesson = course.modules[0].lessons[0]

        val discussion = submitGetDiscussion(lesson.id, DiscussionTopic.LESSON)

        assertThat(discussion).isNotNull()
        assertThat(discussion.id).isNull()
        assertThat(discussion.entityId).isEqualTo(lesson.id)

    }

    @Test
    fun noDiscussionYet_createNewMessage_returnsMessage() {

        val course = testSnapshotService.buildCourseSnapshot(pythonId)
        val lesson = course.modules[0].lessons[0]

        val discussion = submitGetDiscussion(lesson.id, DiscussionTopic.LESSON)

        assertThat(discussion).isNotNull()
        assertThat(discussion.id).isNull()
        assertThat(discussion.entityId).isEqualTo(lesson.id)

        val newMessage = CreateDiscussionMessageRequest(
            entityId = lesson.id,
            discussionTopic = DiscussionTopic.LESSON,
            parentId = null,
            content = "I am a new message as a test"
        )

        val res = submitPostMessage(newMessage)

        assertThat(res.discussionId).isNotNull()
        assertThat(res.parentId).isNull()
        assertThat(res.authorId).isEqualTo(user1.id)
        assertThat(res.content).isEqualTo(newMessage.content)

        val refreshedDiscussion = submitGetDiscussion(lesson.id, DiscussionTopic.LESSON)
        assertThat(refreshedDiscussion.id).isNotNull()
        assertThat(refreshedDiscussion.id).isEqualTo(res.discussionId)
        assertThat(refreshedDiscussion.children.size).isEqualTo(1)
        assertThat(refreshedDiscussion.children[0].id).isEqualTo(res.id)

    }

    @Test
    fun createMessageForWrongTopic_throwsError () {
        val course = testSnapshotService.buildCourseSnapshot(pythonId)
        val lesson = course.modules[0].lessons[0]
        val newMessage = CreateDiscussionMessageRequest(
            entityId = lesson.id,
            discussionTopic = DiscussionTopic.PROJECT,
            parentId = null,
            content = "I am a new message as a test"
        )
        assertErrorOnPostMessage(newMessage, ErrorCode.ENTITY_NOT_FOUND)
    }

    @Test
    fun discussionExists_createReply_returnsMessage() {

        val course = testSnapshotService.buildCourseSnapshot(pythonId)
        val lesson = course.modules[0].lessons[0]

        val discussion = submitGetDiscussion(lesson.id, DiscussionTopic.LESSON)

        val newMessage = CreateDiscussionMessageRequest(
            entityId = lesson.id,
            discussionTopic = DiscussionTopic.LESSON,
            parentId = null,
            content = "I am a new message as a test"
        )

        val res = submitPostMessage(newMessage)

        val replyMessage = CreateDiscussionMessageRequest(
            entityId = lesson.id,
            discussionTopic = DiscussionTopic.LESSON,
            parentId = res.id,
            content = "I am a reply to that other lesson"
        )

        val replyRes = submitPostMessage(replyMessage)
        assertThat(replyRes).isNotNull()
        assertThat(replyRes.discussionId).isEqualTo(res.discussionId)
        assertThat(replyRes.content).isEqualTo(replyMessage.content)
        assertThat(replyRes.parentId).isEqualTo(res.id)

        val refreshedDiscussion = submitGetDiscussion(lesson.id, DiscussionTopic.LESSON)
        assertThat(refreshedDiscussion.id).isEqualTo(replyRes.discussionId)
        assertThat(refreshedDiscussion.children.size).isEqualTo(2)
        val ids = refreshedDiscussion.children.map { it.id }
        assertThat(ids).containsExactlyInAnyOrder(res.id, replyRes.id)
    }



    private fun submitGetDiscussion(entityId: UUID, discussionTopic: DiscussionTopic) : DiscussionResponse {
        return TestRestClient.getOk(url = ApiPaths.DISCUSSION.byEntityIdAndTopic(entityId, topic = discussionTopic), userId = user1.id, responseType = DiscussionResponse::class.java)
    }
    private fun submitPostMessage(req: CreateDiscussionMessageRequest) : DiscussionMessageResponse {
        return TestRestClient.postOk(url = ApiPaths.DISCUSSION.BASE, user1.id, req, DiscussionMessageResponse::class.java)
    }

    private fun assertErrorOnPostMessage(req: CreateDiscussionMessageRequest, errorCode: ErrorCode): ValidatableResponse? =
        TestRestClient.assertError("POST", ApiPaths.DISCUSSION.BASE, user1.id, req, errorCode)


}