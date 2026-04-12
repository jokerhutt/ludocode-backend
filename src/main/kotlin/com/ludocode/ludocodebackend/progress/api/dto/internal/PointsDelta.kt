package com.ludocode.ludocodebackend.progress.api.dto.internal

import com.ludocode.ludocodebackend.progress.domain.enums.CoinTransactionType
import java.util.*

data class PointsDelta(
    val userId: UUID,
    val pointsDelta: Int = 0,
    val transactionType: CoinTransactionType,
    val referenceId: UUID? = null,
)
