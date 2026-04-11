package com.ludocode.ludocodebackend.progress.infra.repository

import com.ludocode.ludocodebackend.progress.domain.entity.XpTransaction
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface XpTransactionRepository : JpaRepository<XpTransaction, UUID> {

    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<XpTransaction>
}

