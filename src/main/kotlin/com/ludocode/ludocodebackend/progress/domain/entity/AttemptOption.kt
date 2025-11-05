package com.ludocode.ludocodebackend.progress.domain.entity

import com.ludocode.ludocodebackend.progress.domain.entity.embedded.AttemptOptionId
import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "attempt_option")
class AttemptOption (
        @EmbeddedId
        val attemptOptionId: AttemptOptionId
    )