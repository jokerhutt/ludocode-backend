package com.ludocode.ludocodebackend.auth.app.port

import com.ludocode.ludocodebackend.auth.api.dto.GoogleTokenResponse

interface GoogleAuthOutboundPort {
    fun exchangeCodeForAccessToken(code: String): GoogleTokenResponse
}