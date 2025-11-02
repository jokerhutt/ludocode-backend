package com.ludocode.ludocodebackend.catalog.domain.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "course")
class Course (

    @Id
    val id: UUID? = null,

    @Column(name = "course_name", nullable = false, unique = true)
    val title: String? = null,



)
