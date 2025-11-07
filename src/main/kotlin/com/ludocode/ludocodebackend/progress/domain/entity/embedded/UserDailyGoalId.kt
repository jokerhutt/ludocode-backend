package com.ludocode.ludocodebackend.progress.domain.entity.embedded

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.LocalDate
import java.util.UUID

@Embeddable
data class UserDailyGoalId (

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "local_date")
    val localDate: LocalDate

)