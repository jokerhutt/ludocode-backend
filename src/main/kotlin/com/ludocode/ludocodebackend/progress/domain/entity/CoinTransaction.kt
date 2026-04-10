package com.ludocode.ludocodebackend.progress.domain.entity

import com.ludocode.ludocodebackend.progress.domain.enums.CoinTransactionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "coin_transaction")
class CoinTransaction(

    @Id
    @Column(name = "id")
    val id: UUID? = null,

    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "amount")
    val amount: Int,

    @Column(name = "balance_after")
    val balanceAfter: Int,

    @Column(name = "transaction_type")
    @Enumerated(EnumType.STRING)
    val transactionType: CoinTransactionType,

    @Column(name = "reference_id")
    val referenceId: UUID? = null,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime
)