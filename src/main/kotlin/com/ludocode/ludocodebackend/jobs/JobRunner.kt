package com.ludocode.ludocodebackend.jobs

import com.ludocode.ludocodebackend.commons.constants.JobNames
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class JobRunner(
    private val monthlyCreditResetJob: MonthlyCreditResetJob
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(JobRunner::class.java)

    override fun run(args: ApplicationArguments) {

        val jobName = args.getOptionValues("job")?.firstOrNull()
            ?: return

        val start = System.currentTimeMillis()

        logger.info(
            LogEvents.JOB_STARTED + " {}",
            kv(LogFields.JOB_NAME, jobName)
        )

        try {
            when (jobName) {
                JobNames.MONTHLY_CREDIT_RESET -> {
                    monthlyCreditResetJob.execute()
                }

                else -> {
                    logger.error(
                        LogEvents.JOB_UNKNOWN + " {}",
                        kv(LogFields.JOB_NAME, jobName)
                    )
                    throw IllegalArgumentException("Unknown job: $jobName")
                }
            }

            val durationMs = System.currentTimeMillis() - start

            logger.info(
                LogEvents.JOB_COMPLETED + " {} {}",
                kv(LogFields.JOB_NAME, jobName),
                kv(LogFields.DURATION_MS, durationMs)
            )

            exitProcess(0)

        } catch (ex: Exception) {

            val durationMs = System.currentTimeMillis() - start

            logger.error(
                LogEvents.JOB_FAILED + " {} {} {}",
                kv(LogFields.JOB_NAME, jobName),
                kv(LogFields.DURATION_MS, durationMs),
                kv(LogFields.ERROR_CODE, ex.message ?: "unknown"),
                ex
            )

            exitProcess(1)
        }
    }
}