package com.ludocode.ludocodebackend.catalog.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "exercise")
class Lesson (

    @Id
    val id: UUID,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false
)