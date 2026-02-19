package com.ludocode.ludocodebackend.ai.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "user_ai_credits")
class UserAICredits(

    @Id
    val userId: UUID,

    @Column(name = "credits")
    var credits: Int


)