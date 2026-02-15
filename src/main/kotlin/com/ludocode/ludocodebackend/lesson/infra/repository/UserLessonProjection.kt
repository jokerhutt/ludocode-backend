package com.ludocode.ludocodebackend.lesson.infra.repository

import java.util.UUID

interface UserLessonProjection {

    fun getId(): UUID
    fun getTitle(): String
    fun getOrderIndex(): Int
    fun getIsCompleted(): Boolean

}