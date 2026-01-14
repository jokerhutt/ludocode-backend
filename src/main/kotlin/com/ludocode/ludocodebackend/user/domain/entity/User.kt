package com.ludocode.ludocodebackend.user.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.SQLRestriction
import org.hibernate.annotations.UuidGenerator
import org.hibernate.annotations.Where
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.TimeZone
import java.util.UUID

@Entity
@Table(name = "ludo_user")
@SQLRestriction("is_deleted = false")
class User (

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(name = "display_name")
    var displayName: String? = null,

    @Column(name = "avatar_version")
    var avatarVersion: String = "v1",

    @Column(name = "avatar_index")
    var avatarIndex: Int = 1,

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

    @Column(name = "time_zone")
    val timeZone: String = "UTC",

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false

)
