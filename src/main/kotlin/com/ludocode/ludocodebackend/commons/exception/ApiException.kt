package com.ludocode.ludocodebackend.commons.exception


class ApiException(
    val code: ErrorCode,
    override val message: String = code.defaultMessage
) : RuntimeException(message)