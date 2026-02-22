package com.ludocode.ludocodebackend.jobs

import com.ludocode.ludocodebackend.commons.constants.JobNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ProjectCleanupScheduler(
    private val projectCleanupJob: ProjectCleanupJob
) {

    private val logger = LoggerFactory.getLogger(ProjectCleanupScheduler::class.java)

    @Scheduled(
        cron = "\${app.jobs.project-cleanup.cron}",
        zone = "\${app.jobs.project-cleanup.zone}"
    )
    fun runProjectCleanup() {

        val jobName = JobNames.PROJECT_CLEANUP
        val start = System.currentTimeMillis()

        logger.info(
            LogEvents.JOB_STARTED + " {}",
            kv(LogFields.JOB_NAME, jobName)
        )

        try {
            projectCleanupJob.execute()

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