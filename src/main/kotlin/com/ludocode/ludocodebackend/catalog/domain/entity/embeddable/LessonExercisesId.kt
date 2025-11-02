package com.ludocode.ludocodebackend.catalog.domain.entity.embeddable

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class LessonExercisesId(
    @Column(name = "id")
    val id: UUID,

    @Column(name = "order_index", nullable = false, unique = true)
    val orderIndex: Int
) : java.io.Serializable