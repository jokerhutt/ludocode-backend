package com.ludocode.ludocodebackend.banner.integration

import com.ludocode.ludocodebackend.banner.api.dto.BannerActiveRequest
import com.ludocode.ludocodebackend.banner.api.dto.BannerMetadata
import com.ludocode.ludocodebackend.banner.api.dto.CreateBannerRequest
import com.ludocode.ludocodebackend.banner.domain.enums.BannerType
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.jobs.BannerCleanupJob
import com.ludocode.ludocodebackend.support.AbstractIntegrationTest
import com.ludocode.ludocodebackend.support.TestRestClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class BannerIT : AbstractIntegrationTest() {

    @Autowired
    lateinit var bannerCleanupJob: BannerCleanupJob

    @Test
    fun createBanner_returnsBannerInAdminAndPublicLists() {
        val adminList = submitCreateBanner(
            CreateBannerRequest(
                type = BannerType.MAINTENANCE,
                text = "Planned maintenance tonight"
            )
        )

        assertThat(adminList).hasSize(1)
        assertThat(adminList.first().isActive).isTrue()
        assertThat(adminList.first().type).isEqualTo(BannerType.MAINTENANCE)

        val publicList = submitGetActiveBanners()
        assertThat(publicList).hasSize(1)
        assertThat(publicList.first().text).isEqualTo("Planned maintenance tonight")
    }

    @Test
    fun createBanner_sameTypeWhenActive_throwsConflictError() {
        submitCreateBanner(
            CreateBannerRequest(
                type = BannerType.FEATURE,
                text = "Feature banner A"
            )
        )

        TestRestClient.assertError(
            "POST",
            ApiPaths.BANNERS.ADMIN_BASE,
            user1.id,
            CreateBannerRequest(
                type = BannerType.FEATURE,
                text = "Feature banner B"
            ),
            ErrorCode.ACTIVE_BANNER_EXISTS
        )
    }

    @Test
    fun deleteBanner_softDeletesBanner_andAllowsRecreateSameType() {
        val created = submitCreateBanner(
            CreateBannerRequest(
                type = BannerType.INCIDENT,
                text = "Incident 1"
            )
        ).first()

        val afterDelete = submitDeleteBanner(created.id)
        val deleted = afterDelete.first { it.id == created.id }

        assertThat(deleted.isActive).isFalse()
        assertThat(submitGetActiveBanners()).isEmpty()

        val recreated = submitCreateBanner(
            CreateBannerRequest(
                type = BannerType.INCIDENT,
                text = "Incident 2"
            )
        )

        assertThat(recreated.count { it.type == BannerType.INCIDENT && it.isActive }).isEqualTo(1)
    }

    @Test
    fun bannerCleanupJob_deactivatesExpiredActiveBanners() {
        val baseTime = Instant.parse("2026-01-01T10:00:00Z")
        clock.set(baseTime)

        submitCreateBanner(
            CreateBannerRequest(
                type = BannerType.MAINTENANCE,
                text = "Short lived",
                expiresAt = baseTime.plusSeconds(60)
            )
        )

        assertThat(submitGetActiveBanners()).hasSize(1)

        clock.set(baseTime.plusSeconds(120))
        val deactivatedCount = bannerCleanupJob.execute()

        assertThat(deactivatedCount).isEqualTo(1)
        assertThat(submitGetActiveBanners()).isEmpty()
    }

    private fun submitCreateBanner(req: CreateBannerRequest): Array<BannerMetadata> =
        TestRestClient.postOk(ApiPaths.BANNERS.ADMIN_BASE, user1.id, req, Array<BannerMetadata>::class.java)

    private fun submitDeleteBanner(bannerId: Long): Array<BannerMetadata> =
        TestRestClient.deleteOk(ApiPaths.BANNERS.byIdAdmin(bannerId), user1.id, Array<BannerMetadata>::class.java)

    private fun submitGetActiveBanners(): Array<BannerActiveRequest> =
        TestRestClient.getOk(ApiPaths.BANNERS.BASE, user1.id, Array<BannerActiveRequest>::class.java)
}

