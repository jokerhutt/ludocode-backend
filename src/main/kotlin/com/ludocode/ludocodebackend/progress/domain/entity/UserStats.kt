package com.ludocode.ludocodebackend.progress.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "user_stats")
class UserStats (

    @Id
    @Column(name = "user_id")
    var userId: UUID,

    @Column(nullable = false) var coins: Int = 0,
    @Column(nullable = false) var streak: Int = 0

)