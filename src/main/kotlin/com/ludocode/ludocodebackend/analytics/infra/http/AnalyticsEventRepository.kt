package com.ludocode.ludocodebackend.analytics.infra.http

import com.ludocode.ludocodebackend.analytics.domain.entity.AnalyticsEvent
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AnalyticsEventRepository : JpaRepository<AnalyticsEvent, UUID> {



}