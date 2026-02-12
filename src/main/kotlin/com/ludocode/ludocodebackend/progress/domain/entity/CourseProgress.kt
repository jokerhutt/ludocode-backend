package com.ludocode.ludocodebackend.progress.domain.entity

import com.ludocode.ludocodebackend.progress.domain.entity.embedded.CourseProgressId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "course_progress")
class CourseProgress (

    @EmbeddedId
    val id: CourseProgressId,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime,

    @Column(name = "current_module_id")
    var currentModuleId: UUID,

    @Column(nullable = false, name = "is_complete")
    var isComplete: Boolean = false

)