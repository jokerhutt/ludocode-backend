package com.ludocode.ludocodebackend.auth.app.port.out

import com.ludocode.ludocodebackend.auth.api.dto.GoogleTokenResponse

interface GoogleAuthOutboundPort {
    fun exchangeCodeForAccessToken(code: String): GoogleTokenResponse
}