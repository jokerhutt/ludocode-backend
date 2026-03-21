package com.ludocode.ludocodebackend.banner.app.service

import com.ludocode.ludocodebackend.banner.api.dto.BannerActiveRequest
import com.ludocode.ludocodebackend.banner.api.dto.BannerMetadata
import com.ludocode.ludocodebackend.banner.api.dto.CreateBannerRequest
import com.ludocode.ludocodebackend.banner.domain.entity.Banner
import com.ludocode.ludocodebackend.banner.infra.repository.BannerRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class BannerService(
	private val bannerRepository: BannerRepository,
	private val clock: Clock
) {

	@Transactional
	fun createBanner(req: CreateBannerRequest): List<BannerMetadata> {
		val text = req.text.trim()
		if (text.isBlank()) {
			throw ApiException(ErrorCode.BAD_REQ, "Banner text can not be blank")
		}

		val now = Instant.now(clock)
		req.expiresAt?.let {
			if (!it.isAfter(now)) {
				throw ApiException(ErrorCode.BAD_REQ, "Banner expiry must be in the future")
			}
		}

		if (bannerRepository.existsByTypeAndIsActiveTrue(req.type)) {
			throw ApiException(ErrorCode.ACTIVE_BANNER_EXISTS)
		}

		try {
			bannerRepository.save(
				Banner(
					type = req.type,
					text = text,
					isActive = true,
					expiresAt = req.expiresAt,
					createdAt = now
				)
			)
		} catch (_: DataIntegrityViolationException) {
			// Enforce the same rule on race conditions where two requests create the same active type.
			throw ApiException(ErrorCode.ACTIVE_BANNER_EXISTS)
		}

		return getAllBanners()
	}

	fun getAllBanners(): List<BannerMetadata> =
		bannerRepository.findAllByOrderByCreatedAtDesc().map { it.toMetadata() }

	fun getActiveBanners(): List<BannerActiveRequest> =
		bannerRepository.findAllByIsActiveTrueOrderByCreatedAtDesc().map { it.toPublicDto() }

	@Transactional
	fun deleteBanner(bannerId: Long): List<BannerMetadata> {
		val banner = bannerRepository.findById(bannerId)
			.orElseThrow { ApiException(ErrorCode.BANNER_NOT_FOUND) }

		banner.isActive = false
		return getAllBanners()
	}

	@Transactional
	fun deactivateExpiredBanners(): Int {
		val now = Instant.now(clock)
		val expired = bannerRepository.findAllByIsActiveTrueAndExpiresAtIsNotNullAndExpiresAtLessThanEqual(now)

		expired.forEach { it.isActive = false }
		return expired.size
	}

	private fun Banner.toMetadata() = BannerMetadata(
		id = id,
		type = type,
		text = text,
		isActive = isActive,
		expiresAt = expiresAt,
		createdAt = createdAt
	)

	private fun Banner.toPublicDto() = BannerActiveRequest(
		id = id,
		type = type,
		text = text
	)
}