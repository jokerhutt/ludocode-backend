package com.ludocode.ludocodebackend.banner.api.dto

import com.ludocode.ludocodebackend.banner.domain.enums.BannerType

data class BannerActiveRequest(
	val id: Long,
	val type: BannerType,
	val text: String
)

