package com.ludocode.ludocodebackend.auth.app.service

import com.ludocode.ludocodebackend.auth.app.port.out.GoogleAuthOutboundPort
import com.ludocode.ludocodebackend.auth.app.port.out.UserPortForAuth
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
    private val authCookieService: AuthCookieService
) {

    fun loginWithGoogle(code: String, response: HttpServletResponse): UserResponse {

        // 1) Exchange code with Google for tokens
        val googleTokens = googleAuth.exchangeCodeForAccessToken(code)

        // 2) Parse Google ID token (sub, email, name, avatar)
        val claims = SignedJWT.parse(googleTokens.idToken).jwtClaimsSet

        val providerSub = claims.subject // Google user ID
        val email = claims.getStringClaim("email")
        val firstName = claims.getStringClaim("given_name")
        val lastName = claims.getStringClaim("family_name")
        val avatar = claims.getStringClaim("picture")

        // 3) Ask User microservice to findOrCreate the user
        val user = userPortForAuth.findOrCreate(
            FindOrCreateUserRequest(
                provider = AuthProvider.GOOGLE,
                providerUserId = providerSub,
                email = email,
                firstName = firstName,
                lastName = lastName,
                name = firstName + lastName,
                avatarUrl = avatar
            )
        )

        // 4) Sign JWT + set cookie
        val jwt = jwtService.createToken(user.id)
        authCookieService.setJwt(response, jwt)

        return user
    }

    fun getAuthenticatedUser (id: UUID) : UserResponse {
        return userPortForAuth.getById(id)
    }







}