package com.ludocode.ludocodebackend.catalog.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "module")
class Module (

    @Id
    val id: UUID,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "course_id", nullable = false)
    val courseId: UUID,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean,

    @Column(name = "order_index")
    val orderIndex: Int

    )