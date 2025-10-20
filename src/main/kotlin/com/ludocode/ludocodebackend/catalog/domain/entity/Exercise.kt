package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.catalog.domain.enums.ExerciseType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "exercise")
class Exercise (

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "prompt")
    var prompt: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false)
    var exerciseType: ExerciseType,

    @Column(name = "lessonId")
    var lessonId: UUID? = null

    )