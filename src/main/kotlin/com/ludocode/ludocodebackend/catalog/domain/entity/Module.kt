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
    var title: String,

    @Column(name = "course_id", nullable = false)
    val courseId: UUID,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean? = false,

    @Column(name = "order_index")
    var orderIndex: Int

    )