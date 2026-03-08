package com.ludocode.ludocodebackend.languages.entity

import jakarta.persistence.*

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

    @Column(name = "runtime_version")
    var runtimeVersion: String = "*",

    @Column(name = "initial_script", nullable = true)
    var initialScript: String = "",

    @Column(name = "editor_id", nullable = false, unique = true)
    var editorId: String,

    @Column(name = "piston_id", nullable = false, unique = true)
    var pistonId: String,

    @Column(name = "base", nullable = false)
    var base: String,

    @Column(name = "icon_name", nullable = false)
    var iconName: String,

    @Column(name = "extension", nullable = false)
    var extension: String

)