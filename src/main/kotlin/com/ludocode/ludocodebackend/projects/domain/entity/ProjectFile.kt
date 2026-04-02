package com.ludocode.ludocodebackend.projects.domain.entity
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

    @Column(name = "file_path")
    var filePath: String,

    @Column(name = "code_language")
    val codeLanguage: String

)