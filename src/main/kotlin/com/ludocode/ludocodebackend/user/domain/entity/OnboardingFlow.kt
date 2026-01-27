package com.ludocode.ludocodebackend.user.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "onboarding_flow")
class OnboardingFlow(

    @Id
    @GeneratedValue
    var id: UUID? = null,

    @Column(name = "flow_key", nullable = false)
    var key: String = "",

    @Column(name = "version", nullable = false)
    var version: Int = 1,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
)