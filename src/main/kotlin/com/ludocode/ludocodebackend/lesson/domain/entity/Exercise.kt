package com.ludocode.ludocodebackend.lesson.domain.entity

import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "exercise")
class Exercise (

    @EmbeddedId
    val exerciseId: ExerciseId = ExerciseId(UUID.randomUUID(), 1),

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "prompt")
    var prompt: String? = null,

    @Column(name = "subtitle")
    var subtitle: String? = null,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "exercise_type", nullable = false)
    var exerciseType: ExerciseType,

    @Column(name = "exercise_media")
    val exerciseMedia: String? = null,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,

    )