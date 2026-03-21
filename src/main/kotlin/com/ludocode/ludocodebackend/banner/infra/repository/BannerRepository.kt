package com.ludocode.ludocodebackend.banner.infra.repository

import com.ludocode.ludocodebackend.banner.domain.entity.Banner
import com.ludocode.ludocodebackend.banner.domain.enums.BannerType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface BannerRepository : JpaRepository<Banner, Long> {
	fun existsByTypeAndIsActiveTrue(type: BannerType): Boolean

	fun findAllByOrderByCreatedAtDesc(): List<Banner>

	fun findAllByIsActiveTrueOrderByCreatedAtDesc(): List<Banner>

	fun findAllByIsActiveTrueAndExpiresAtIsNotNullAndExpiresAtLessThanEqual(now: Instant): List<Banner>
}