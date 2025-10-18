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
@Table(name = "course")
class Course (

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @UuidGenerator
    val id: UUID? = null,

    @Column(name = "title")
    val title: String? = null,

)
