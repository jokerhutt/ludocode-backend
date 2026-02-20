package com.ludocode.ludocodebackend.ai.app.port.out

import org.springframework.stereotype.Component
import java.util.UUID

@Component
interface AiCreditPortForSubscription {

    fun resetCredits(userId: UUID, amount: Int)

}