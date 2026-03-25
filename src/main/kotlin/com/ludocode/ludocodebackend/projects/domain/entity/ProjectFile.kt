package com.ludocode.ludocodebackend.projects.domain.entity

import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "project_file")
class ProjectFile(

    @Id
    val id: UUID,

    @Column(name = "project_id")
    val projectId: UUID,

    @Column(name = "content_url")
    var contentUrl: String,

    @Column(name = "content_hash")
    var contentHash: String,

    @Column(name = "file_path")
    var filePath: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_language_id", nullable = false)
    val codeLanguage: CodeLanguages

)