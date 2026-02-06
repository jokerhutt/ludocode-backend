package com.ludocode.ludocodebackend.catalog.domain.entity

import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_language_id")
    var codeLanguage: CodeLanguages? = null
)