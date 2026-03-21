package com.ludocode.ludocodebackend.banner.api.dto

import com.ludocode.ludocodebackend.banner.domain.enums.BannerType
import java.time.Instant

data class BannerMetadata(
	val id: Long,
	val type: BannerType,
	val text: String,
	val isActive: Boolean,
	val expiresAt: Instant?,
	val createdAt: Instant
)

