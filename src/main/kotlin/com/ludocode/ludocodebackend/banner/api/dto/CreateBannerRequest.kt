package com.ludocode.ludocodebackend.banner.api.dto

import com.ludocode.ludocodebackend.banner.domain.enums.BannerType
import java.time.Instant

data class CreateBannerRequest(
	val type: BannerType,
	val text: String,
	val expiresAt: Instant? = null
)

