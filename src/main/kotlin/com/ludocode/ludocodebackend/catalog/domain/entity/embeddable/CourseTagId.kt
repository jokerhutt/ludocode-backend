package com.ludocode.ludocodebackend.catalog.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class CourseTagId(

    @Column(name = "course_id")
    val courseId: UUID,

    @Column(name = "tag_id")
    val tagId: Long
)