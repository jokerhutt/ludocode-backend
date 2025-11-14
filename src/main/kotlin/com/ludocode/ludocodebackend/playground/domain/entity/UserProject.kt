package com.ludocode.ludocodebackend.playground.domain.entity

import com.ludocode.ludocodebackend.playground.domain.enums.LanguageType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "user_project")
class UserProject (

    @Id
    val id : UUID,

    @Column(name = "name")
    val name : String,

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "request_hash")
    val requestHash: UUID,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "project_language")
    val projectLanguage: LanguageType,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime? = null

)