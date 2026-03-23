package com.ludocode.ludocodebackend.discussion.domain.entity

import com.ludocode.ludocodebackend.discussion.domain.enums.DiscussionTopic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "discussion")
class Discussion (

    @Id
    var id: UUID,

    @Column(name = "entity_id")
    val entityId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "discussion_topic")
    val discussionTopic: DiscussionTopic

    )