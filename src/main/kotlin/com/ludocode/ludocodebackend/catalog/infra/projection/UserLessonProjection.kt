package com.ludocode.ludocodebackend.catalog.infra.projection

import java.time.OffsetDateTime
import java.util.UUID

interface UserLessonProjection {

    fun getId(): UUID
    fun getTitle(): String
    fun getOrderIndex(): Int
    fun getIsCompleted(): Boolean

}