package com.ludocode.ludocodebackend.auth.app.service

import com.ludocode.ludocodebackend.auth.api.dto.UserLoginResponse
import com.ludocode.ludocodebackend.auth.app.port.out.FirebaseAuthPort
import com.ludocode.ludocodebackend.auth.configuration.DemoConfig
import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.commons.logging.withMdc
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserCoinsPortForAuth
import com.ludocode.ludocodebackend.progress.app.port.`in`.UserStreakPortForAuth
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForAuth
import com.ludocode.ludocodebackend.user.domain.enums.AuthProvider
import com.ludocode.ludocodebackend.user.domain.event.UserRegisteredEvent
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val userPortForAuth: UserPortForAuth,
    private val jwtService: JwtService,
    private val authCookieService: AuthCookieService,
    private val userCoinsPortForAuth: UserCoinsPortForAuth,
    private val userStreakPortForAuth: UserStreakPortForAuth,
    private val demoConfig: DemoConfig,
    private val firebaseAuthPort: FirebaseAuthPort,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Autowired
    lateinit var self: AuthService

    internal fun loginWithFirebase(response: HttpServletResponse, token: String): UserLoginResponse {

        return try {
            val decoded = firebaseAuthPort.verifyIdToken(token)

            logger.info(
                LogEvents.AUTH_FIREBASE_VERIFIED + " {}",
                kv(LogFields.PROVIDER_USER_ID, decoded.uid)
            )

            val request = FindOrCreateUserRequest(
                provider = AuthProvider.FIREBASE,
                providerUserId = decoded.uid,
                email = decoded.email
                    ?: throw ApiException(ErrorCode.BAD_REQ, "Email missing from Firebase token"),
                displayName = decoded.name,
                avatarUrl = decoded.picture,
                role = decoded.role
            )
            self.buildLoginResponse(request, response)
        } catch (e: Exception) {
            logger.warn(LogEvents.AUTH_FIREBASE_FAILED, e)
            throw e
        }


    }

    fun loginWithDemo(response: HttpServletResponse): UserLoginResponse {
        logger.info(LogEvents.AUTH_DEMO_LOGIN_REQUESTED)
        val request = FindOrCreateUserRequest(
            provider = AuthProvider.DEMO,
            providerUserId = demoConfig.userId.toString(),
            email = "demo@ludocode.app",
            displayName = "Demo User",
            avatarUrl = null,
            role = null
        )
        return self.buildLoginResponse(request, response)
    }

    @Transactional
     fun buildLoginResponse(
        request: FindOrCreateUserRequest,
        response: HttpServletResponse
    ): UserLoginResponse {
        val user = userPortForAuth.findOrCreate(request)
        applicationEventPublisher.publishEvent(
            UserRegisteredEvent(user.id)
        )

        return withMdc(LogFields.USER_ID to user.id.toString(), LogFields.PROVIDER to request.provider.toString()) {
            logger.info(LogEvents.AUTH_LOGIN_SUCCESS)
            val coins = userCoinsPortForAuth.findOrCreateCoins(user.id)
            val streak = userStreakPortForAuth.getStreak(user.id)
            val jwt = jwtService.createToken(user.id, role = request.role)
            authCookieService.setJwt(response, jwt)
            UserLoginResponse(user, coins, streak)
        }

    }

    internal fun getAuthenticatedUser(id: UUID): UserResponse {
        return userPortForAuth.getById(id)
    }

}