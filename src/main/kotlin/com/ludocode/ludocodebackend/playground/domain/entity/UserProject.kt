package com.ludocode.ludocodebackend.playground.domain.entity

import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.playground.domain.enums.Visibility
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

    @Column(name = "delete_at")
    var deleteAt: OffsetDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_language_id", nullable = false)
    val codeLanguage: CodeLanguages,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "project_visibility")
    val projectVisibility: Visibility? = Visibility.PRIVATE,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null

)