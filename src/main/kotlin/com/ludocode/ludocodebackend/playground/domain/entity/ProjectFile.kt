package com.ludocode.ludocodebackend.playground.domain.entity

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

@Entity
@Table(name = "project_file")
class ProjectFile (

    @Id
    val id : UUID,

    @Column(name = "project_id")
    val projectId: UUID,

    @Column(name = "content_url")
    val contentUrl : String,

    @Column(name = "content_hash")
    var contentHash: String,

    @Column(name = "file_path")
    var filePath : String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_language_id", nullable = false)
    val codeLanguage: CodeLanguages

)