package com.ludocode.ludocodebackend.auth.app.service
import com.google.firebase.auth.FirebaseAuth
import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.auth.app.port.out.FirebaseAuthPort
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForAuth
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserCoinsPortForAuth
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserStreakPortForAuth
import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userPortForAuth: UserPortForAuth,
    private val jwtService: JwtService,
    private val authCookieService: AuthCookieService,
    private val userCoinsPortForAuth: UserCoinsPortForAuth,
    private val userStreakPortForAuth: UserStreakPortForAuth,
    private val demoConfig: DemoConfig,
    private val firebaseAuthPort: FirebaseAuthPort
) {

    internal fun loginWithFirebase (response: HttpServletResponse, token: String) : UserLoginResponse {
        val decoded = firebaseAuthPort.verifyIdToken(token)

        val request = FindOrCreateUserRequest(
            provider = AuthProvider.FIREBASE,
            providerUserId = decoded.uid,
            email = decoded.email
                ?: throw ApiException(ErrorCode.BAD_REQ, "Email missing from Firebase token"),
            firstName = decoded.name,
            lastName = null,
            name = decoded.name,
            avatarUrl = decoded.picture
        )
        return buildLoginResponse(request, response)
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
        val streak = userStreakPortForAuth.getStreak(user.id)
        val jwt = jwtService.createToken(user.id)
        authCookieService.setJwt(response, jwt)
        return UserLoginResponse(user, coins, streak)
    }

    internal fun getAuthenticatedUser (id: UUID) : UserResponse {
        return userPortForAuth.getById(id)
    }

}