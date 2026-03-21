package com.ludocode.ludocodebackend.jobs

import com.ludocode.ludocodebackend.banner.app.service.BannerService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component

@Component
class BannerCleanupJob(
    private val bannerService: BannerService
) {

    @Transactional
    fun execute(): Int {
        return bannerService.deactivateExpiredBanners()
    }
}

