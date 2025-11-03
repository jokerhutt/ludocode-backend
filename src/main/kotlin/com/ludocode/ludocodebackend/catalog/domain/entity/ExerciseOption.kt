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
@Table(name = "exercise_option")
class ExerciseOption (

    @Id
    val id: UUID? = null,

    @Column(name = "exercise_id", nullable = false, unique = true)
    var exerciseId: UUID,

    @Column(name = "exercise_version", nullable = false, unique = true)
    var exerciseVersion: Int,

    @Column(name = "option_id", nullable = false)
    var optionId: UUID,

    @Column(name = "answer_order")
    val answerOrder: Int? = null

)