package com.ludocode.ludocodebackend.catalog.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "lesson")
class Lesson (

    @Id
    var id: UUID,

    @Column(name = "title")
    var title: String,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false
)