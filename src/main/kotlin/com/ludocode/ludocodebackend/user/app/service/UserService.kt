package com.ludocode.ludocodebackend.user.app.service

import com.ludocode.ludocodebackend.playground.config.GcsFeatureConfig
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.mapper.UserMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.storage.app.dto.request.MediaPutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequest
import com.ludocode.ludocodebackend.storage.app.dto.request.StoragePutRequestList
import com.ludocode.ludocodebackend.storage.app.port.`in`.StoragePortForServices
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForAuth
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForProgress
import com.ludocode.ludocodebackend.user.domain.entity.ExternalAccount
import com.ludocode.ludocodebackend.user.domain.entity.User
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import com.ludocode.ludocodebackend.user.infra.repository.ExternalAccountRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserPreferencesRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.net.URL
import java.net.URLConnection
import java.time.OffsetDateTime
import java.time.Clock
import java.util.UUID
import javax.imageio.ImageIO

@Service
class UserService(
    private val userRepository: UserRepository,
    private val externalAccountRepository: ExternalAccountRepository,
    private val userMapper: UserMapper,
    private val clock: Clock,
    private val gcsFeatureConfig: GcsFeatureConfig,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val courseProgressPortForUser: CourseProgressPortForUser,
    private val storagePortForServices: StoragePortForServices
) : UserPortForProgress, UserPortForAuth {

    override fun getById(id: UUID): UserResponse {
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(), hasOnboarded(id))
    }

    override fun getUserTimezone(userId: UUID): String? {
        return userRepository.findUserTimeZone(userId)
    }

    @Transactional
    internal fun deleteUser(userId: UUID) {
        val existingUser = userRepository.findById(userId).orElseThrow()




    }

    @Transactional
    override fun findOrCreate(req: FindOrCreateUserRequest): UserResponse {

        val existingUser : ExternalAccount? = externalAccountRepository.findByProviderAndProviderUserId(req.provider, req.providerUserId)

        if (existingUser != null) return getById(existingUser.userId)

        var newUser = userRepository.save(
            User(
                email = req.email ?: "",
                firstName = req.firstName ?: "",
                lastName = req.lastName ?: "",
                pfpSrc = "",
                createdAt = OffsetDateTime.now(clock)
            )
        )

        externalAccountRepository.save(
            ExternalAccount(
                userId = newUser.id!!,
                provider = req.provider,
                providerUserId = req.providerUserId
            )
        )

        val userPfp = saveUserAvatar(newUser.id, req.avatarUrl)
        newUser.pfpSrc = userPfp
        userRepository.save(newUser)

        return userMapper.toUserResponse(newUser, hasOnboarded(newUser.id))
    }

    private fun saveUserAvatar(userId: UUID, avatarUrl: String?): String? {

        if (!gcsFeatureConfig.enabled) return null

        if (avatarUrl.isNullOrBlank()) {
            return "avatars/default.png"
        }

        return try {
            val bytes = URL(avatarUrl).readBytes()

            val mime = URLConnection.guessContentTypeFromStream(bytes.inputStream())
                ?: "application/octet-stream"

            val ext = when (mime) {
                "image/jpeg" -> "jpg"
                "image/png" -> "png"
                "image/webp" -> "webp"
                "image/gif" -> "gif"
                else -> "bin"
            }

            val path = "avatars/$userId.$ext"

            storagePortForServices.uploadMedia(
                MediaPutRequest(path, bytes)
            )

            path

        } catch (e: Exception) {
            println(e)
            "avatars/default.png"
        }
    }

    fun hasOnboarded (userId: UUID) : Boolean {
        val preferencesExist = userPreferencesRepository.existsById(userId)
        val currentCourseExists = courseProgressPortForUser.existsAnyByUserId(userId)
        return preferencesExist && currentCourseExists
    }

    @Transactional
    internal fun createPreferences (submission: OnboardingSubmission, userId: UUID) : OnboardingResponse {
        val toSubmit = UserPreferences(userId = userId, hasExperience = submission.hasProgrammingExperience, chosenPath = submission.chosenPath)
        val savedPreferences = userPreferencesRepository.save(toSubmit)
        val newCourseProgressWithEnrolled = courseProgressPortForUser.findOrCreate(userId, submission.chosenCourse)
        return OnboardingResponse(refreshedUser = getById(userId), savedPreferences, courseProgressResponse = newCourseProgressWithEnrolled)
    }

    internal fun getPreferences (userId: UUID) : UserPreferences {
        val preferences = userPreferencesRepository.findById(userId).orElseThrow()
        return preferences
    }

    internal fun getUsersByIds(userIds: List<UUID>): List<UserResponse> {
        val users = userRepository.findAllByIdIn(userIds)
        val onboardingMap = userIds.associateWith { hasOnboarded(it) }
        return userMapper.toUserResponseList(users, onboardingMap)
    }





}