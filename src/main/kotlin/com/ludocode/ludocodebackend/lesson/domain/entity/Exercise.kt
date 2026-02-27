package com.ludocode.ludocodebackend.lesson.domain.entity

import com.ludocode.ludocodebackend.exercise.Block
import com.ludocode.ludocodebackend.exercise.ExerciseInteraction
import com.ludocode.ludocodebackend.lesson.domain.entity.embeddable.ExerciseId
import com.ludocode.ludocodebackend.lesson.domain.enums.ExerciseType
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Entity
@Table(name = "exercise")
class Exercise(

    @EmbeddedId
    val exerciseId: ExerciseId = ExerciseId(UUID.randomUUID(), 1),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "blocks", columnDefinition = "jsonb", nullable = false)
    val blocks: List<Block>,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "interaction", columnDefinition = "jsonb")
    val interaction: ExerciseInteraction? = null,

    @Column(name = "is_deleted")
    var isDeleted: Boolean? = false

)



