package com.ludocode.ludocodebackend.user.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "ludo_user")
class User (

    @Id
    val id: UUID,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "pfp_src")
    var pfpSrc: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "created_at")
    var createdAt: OffsetDateTime? = null,

)
