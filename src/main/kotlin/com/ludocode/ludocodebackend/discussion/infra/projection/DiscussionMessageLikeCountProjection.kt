package com.ludocode.ludocodebackend.discussion.infra.projection

import java.util.UUID

interface DiscussionMessageLikeCountProjection {
    fun getMessageId(): UUID
    fun getLikeCount(): Long
}

