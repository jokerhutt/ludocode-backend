package com.ludocode.ludocodebackend.runner.api.dto.response

data class RunnerResult(val stdout: String, val stderr: String, val exitCode: Int)