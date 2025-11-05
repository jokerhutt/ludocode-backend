package com.ludocode.ludocodebackend.progress.domain.entity.embedded

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class CourseProgressId(

    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "course_id", nullable = false)
    val courseId: UUID

) : java.io.Serializable