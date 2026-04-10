package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.CoinTransaction
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CoinTransactionRepository : JpaRepository<CoinTransaction, UUID> {

    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<CoinTransaction>
}

