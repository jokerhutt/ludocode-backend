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
@Table(name = "lesson")
class Lesson (

    @Id
    val id: UUID,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "module_id", nullable = false)
    var moduleId: UUID,

    @Column(name = "order_index", nullable = false)
    var orderIndex: Int = 1,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false
)