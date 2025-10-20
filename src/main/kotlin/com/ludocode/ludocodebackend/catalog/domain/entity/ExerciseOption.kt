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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "content")
    var content: String? = null,

    @Column(name = "answer_order")
    var answerOrder: Int? = null,

    @Column(name = "exercise_id")
    val exerciseId: UUID? = null

)