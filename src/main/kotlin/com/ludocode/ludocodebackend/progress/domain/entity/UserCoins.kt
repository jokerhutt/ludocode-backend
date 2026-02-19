package com.ludocode.ludocodebackend.progress.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "user_coins")
class UserCoins(

    @Id
    @Column(name = "user_id")
    var userId: UUID,

    @Column(nullable = false) var coins: Int = 0,

    )