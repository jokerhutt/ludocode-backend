package com.ludocode.ludocodebackend.jobs

import com.ludocode.ludocodebackend.commons.constants.JobNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BannerCleanupScheduler(
    private val bannerCleanupJob: BannerCleanupJob
) {

    private val logger = LoggerFactory.getLogger(BannerCleanupScheduler::class.java)

    @Scheduled(
        cron = "\${app.jobs.banner-cleanup.cron}",
        zone = "\${app.jobs.banner-cleanup.zone}"
    )
    fun runBannerCleanup() {

        val jobName = JobNames.BANNER_CLEANUP
        val start = System.currentTimeMillis()

        logger.info(
            LogEvents.JOB_STARTED + " {}",
            kv(LogFields.JOB_NAME, jobName)
        )

        try {
            bannerCleanupJob.execute()

            val durationMs = System.currentTimeMillis() - start

            logger.info(
                LogEvents.JOB_COMPLETED + " {} {}",
                kv(LogFields.JOB_NAME, jobName),
                kv(LogFields.DURATION_MS, durationMs)
            )

        } catch (ex: Exception) {

            val durationMs = System.currentTimeMillis() - start

            logger.error(
                LogEvents.JOB_FAILED + " {} {} {}",
                kv(LogFields.JOB_NAME, jobName),
                kv(LogFields.DURATION_MS, durationMs),
                kv(LogFields.ERROR_CODE, ex.message ?: "unknown"),
                ex
            )
        }
    }
}

