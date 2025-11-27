package com.ludocode.ludocodebackend.auth.app.service

import com.ludocode.ludocodebackend.auth.api.dto.response.UserLoginResponse
import com.ludocode.ludocodebackend.auth.app.port.out.GoogleAuthOutboundPort
import com.ludocode.ludocodebackend.auth.app.port.out.UserPortForAuth
import com.ludocode.ludocodebackend.auth.app.port.out.UserCoinsPortForAuth
import com.ludocode.ludocodebackend.auth.app.port.out.UserStreakPortForAuth
import com.ludocode.ludocodebackend.auth.config.DemoConfig
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import com.nimbusds.jwt.SignedJWT
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val googleAuth: GoogleAuthOutboundPort,
    private val userPortForAuth: UserPortForAuth,
    private val jwtService: JwtService,
    private val authCookieService: AuthCookieService,
    private val userCoinsPortForAuth: UserCoinsPortForAuth,
    private val userStreakPortForAuth: UserStreakPortForAuth,
    private val demoConfig: DemoConfig
) {

    internal fun loginWithGoogle(code: String, response: HttpServletResponse): UserLoginResponse {

        val googleTokens = googleAuth.exchangeCodeForAccessToken(code)

        val claims = SignedJWT.parse(googleTokens.idToken).jwtClaimsSet

        val providerSub = claims.subject
        val email = claims.getStringClaim("email")
        val firstName = claims.getStringClaim("given_name")
        val lastName = claims.getStringClaim("family_name")
        val avatar = claims.getStringClaim("picture")

        val focRequest =
            FindOrCreateUserRequest(
                provider = AuthProvider.GOOGLE,
                providerUserId = providerSub,
                email = email,
                firstName = firstName,
                lastName = lastName,
                name = firstName + lastName,
                avatarUrl = avatar
            )

        return buildLoginResponse(focRequest, response)
    }

    fun loginWithDemo(response: HttpServletResponse): UserLoginResponse {
        val request = FindOrCreateUserRequest(
            provider = AuthProvider.DEMO,
            providerUserId = demoConfig.userId.toString(),
            email = "demo@ludocode.app",
            firstName = "Demo",
            lastName = "User",
            name = "Demo User",
            avatarUrl = null
        )

        return buildLoginResponse(request, response)
    }

    private fun buildLoginResponse(
        request: FindOrCreateUserRequest,
        response: HttpServletResponse
    ): UserLoginResponse {

        val user = userPortForAuth.findOrCreate(request)

        val coins = userCoinsPortForAuth.findOrCreateCoins(user.id)
        val streak = userStreakPortForAuth.upsertStreak(user.id)

        val jwt = jwtService.createToken(user.id)
        authCookieService.setJwt(response, jwt)

        return UserLoginResponse(user, coins, streak)
    }


    internal fun getAuthenticatedUser (id: UUID) : UserResponse {
        return userPortForAuth.getById(id)
    }

}