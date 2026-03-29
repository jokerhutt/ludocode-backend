package com.ludocode.ludocodebackend.projects.domain.entity

import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.projects.domain.enums.ProjectType
import com.ludocode.ludocodebackend.projects.domain.enums.Visibility
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "user_project")
class UserProject(

    @Id
    val id: UUID,

    @Column(name = "name")
    var name: String,

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "request_hash")
    val requestHash: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type")
    val projectType: ProjectType,

    @Column(name = "delete_at")
    var deleteAt: OffsetDateTime? = null,

    @Column(name = "entry_file_path")
    var entryFilePath: String? = null,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "project_visibility")
    var projectVisibility: Visibility = Visibility.PRIVATE,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null

)