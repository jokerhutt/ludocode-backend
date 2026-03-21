package com.ludocode.ludocodebackend.banner.api.dto

@Deprecated("Banners can not be updated after creation. Delete and create a new banner instead.")
data class UpdateBannerRequest(
	val ignored: String? = null
)

