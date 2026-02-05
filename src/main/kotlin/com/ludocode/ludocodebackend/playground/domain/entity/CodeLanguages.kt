package com.ludocode.ludocodebackend.playground.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "code_languages",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["slug"])
    ]
)
class CodeLanguages(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var slug: String,

    @Column(nullable = false)
    var name: String,

    @Column(name = "initial_script", nullable = true)
    var initialScript: String = "",

    @Column(name = "editor_id", nullable = false, unique = true)
    var editorId: String
)