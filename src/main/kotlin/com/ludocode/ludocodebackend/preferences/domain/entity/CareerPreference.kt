package com.ludocode.ludocodebackend.preferences.domain.entity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "career_preferences")
class CareerPreference (

    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID(),

    @Column(name = "choice", unique = true)
    val choice: String,

    @Column(name = "title")
    val title: String,

    @Column(name = "description")
    val description: String,

    @Column(name = "course_id")
    val courseId: UUID,

    )

