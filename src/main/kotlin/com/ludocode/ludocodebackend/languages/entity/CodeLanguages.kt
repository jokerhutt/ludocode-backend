package com.ludocode.ludocodebackend.languages.entity

import com.ludocode.ludocodebackend.languages.domain.enums.LanguageRuntime
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

    @Column(nullable = false, unique = true)
    var slug: String,

    @Column(nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "runtime")
    var runtime: LanguageRuntime,

    @Column(name = "runtime_version")
    var runtimeVersion: String = "*",

    @Column(name = "initial_script", nullable = true)
    var initialScript: String = "",

    @Column(name = "editor_id", nullable = false)
    var editorId: String,

    @Column(name = "piston_id", nullable = false)
    var pistonId: String,

    @Column(name = "base", nullable = false)
    var base: String,

    @Column(name = "icon_name", nullable = false)
    var iconName: String,

    @Column(name = "extension", nullable = false)
    var extension: String,

    @Column(name = "is_enabled", nullable = false)
    var isEnabled: Boolean = true,

    @Column(name = "disabled_reason")
    var disabledReason: String? = null

)