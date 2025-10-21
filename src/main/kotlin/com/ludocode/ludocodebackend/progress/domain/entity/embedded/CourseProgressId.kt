package com.ludocode.ludocodebackend.progress.domain.entity.embedded

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class CourseProgressId(
    @Column(name = "user_id", nullable = false)
    var userId: UUID? = null,
    @Column(name = "course_id", nullable = false)
    var courseId: UUID? = null
) : java.io.Serializable