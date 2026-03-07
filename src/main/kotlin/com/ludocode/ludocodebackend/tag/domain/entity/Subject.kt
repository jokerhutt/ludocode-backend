package com.ludocode.ludocodebackend.tag.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

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