package com.ludocode.ludocodebackend.jobs

import com.ludocode.ludocodebackend.commons.constants.JobNames
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class JobRunner(
    private val monthlyCreditResetJob: MonthlyCreditResetJob
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {

        if (args.containsOption("job") &&
            args.getOptionValues("job")?.contains(JobNames.MONTHLY_CREDIT_RESET) == true
        ) {
            monthlyCreditResetJob.execute()
            exitProcess(0)
        }
    }
}