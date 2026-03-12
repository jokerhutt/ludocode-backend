package com.ludocode.ludocodebackend.analytics.app.service

import com.ludocode.ludocodebackend.analytics.api.dto.AnalyticsEventRequest
import com.ludocode.ludocodebackend.analytics.domain.entity.AnalyticsEvent
import com.ludocode.ludocodebackend.analytics.infra.http.AnalyticsEventRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class AnalyticsService(private val analyticsEventRepository: AnalyticsEventRepository) {

    @Transactional
    fun saveEvent (req: AnalyticsEventRequest) {
       analyticsEventRepository.save(
           AnalyticsEvent(
               event = req.event,
               properties = req.properties
           )
       )
    }


}