package com.ludocode.ludocodebackend.lesson.infra.projection

import com.ludocode.ludocodebackend.projects.api.dto.snapshot.ProjectSnapshot
import java.util.*

interface UserLessonProjection {

    fun getId(): UUID
    fun getTitle(): String
    fun getProjectSnapshot(): String?
    fun getOrderIndex(): Int
    fun getIsCompleted(): Boolean
    fun getLessonType(): String

}