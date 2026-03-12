package com.ludocode.ludocodebackend.analytics.app.service

import com.ludocode.ludocodebackend.analytics.api.dto.AnalyticsEventRequest
import com.ludocode.ludocodebackend.analytics.domain.entity.AnalyticsEvent
import com.ludocode.ludocodebackend.analytics.domain.enums.AnalyticsEventKey
import com.ludocode.ludocodebackend.analytics.infra.http.AnalyticsEventRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AnalyticsService(private val analyticsEventRepository: AnalyticsEventRepository) {

    @Transactional
    fun saveEvent (req: AnalyticsEventRequest) {
        val safe = sanitize(req.properties)
       analyticsEventRepository.save(
           AnalyticsEvent(
               event = req.event,
               properties = safe
           )
       )
    }

    private fun sanitize(props: Map<String, Any>?): Map<String, Any> {
        if (props == null) return emptyMap()

        return props.entries
            .take(10) // max props 10
            .associate { (k, v) -> k to v.toString().take(100) } // max len 100
    }


}