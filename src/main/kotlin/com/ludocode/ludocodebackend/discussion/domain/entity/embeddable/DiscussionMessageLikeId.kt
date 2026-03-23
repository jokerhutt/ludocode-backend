package com.ludocode.ludocodebackend.discussion.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
class DiscussionMessageLikeId (

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "message_id")
    val messageId: UUID

    )