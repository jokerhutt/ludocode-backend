package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UuidGenerator
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "exercise")
class Exercise (

    @EmbeddedId
    val exerciseId: ExerciseId,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "prompt")
    var prompt: String? = null,

    @Column(name = "subtitle")
    var subtitle: String? = null,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "exercise_type", nullable = false)
    var exerciseType: ExerciseType,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false,

    )