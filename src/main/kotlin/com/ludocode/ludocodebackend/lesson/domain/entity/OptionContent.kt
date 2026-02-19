package com.ludocode.ludocodebackend.lesson.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "option_content")
class OptionContent(

    @Id
    val id: UUID,

    @Column(name = "content", nullable = false, unique = true)
    var content: String


)