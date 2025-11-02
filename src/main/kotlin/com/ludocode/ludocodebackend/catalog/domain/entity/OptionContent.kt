package com.ludocode.ludocodebackend.catalog.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name="option_content")
class OptionContent (

    @Id
    val id: UUID? = null,

    @Column(name = "content", nullable = false, unique = true)
    var content: String


    )
