package com.ludocode.ludocodebackend.user.domain.entity

import jakarta.persistence.*
import java.util.*

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