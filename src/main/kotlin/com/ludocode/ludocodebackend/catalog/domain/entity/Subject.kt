package com.ludocode.ludocodebackend.catalog.domain.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "subjects",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["slug"])
    ]
)
class Subject(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var slug: String,

    @Column(nullable = false)
    var name: String,

    )