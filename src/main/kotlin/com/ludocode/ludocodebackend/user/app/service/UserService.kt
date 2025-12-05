package com.ludocode.ludocodebackend.user.app.service

import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.mapper.UserMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
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
import java.time.OffsetDateTime
import java.time.Clock
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val externalAccountRepository: ExternalAccountRepository,
    private val userMapper: UserMapper,
    private val clock: Clock,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val courseProgressPortForUser: CourseProgressPortForUser,
) : UserPortForProgress, UserPortForAuth {

    override fun getById(id: UUID): UserResponse {
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(), hasOnboarded(id))
    }

    override fun getUserTimezone(userId: UUID): String? {
        return userRepository.findUserTimeZone(userId)
    }

    @Transactional
    override fun findOrCreate(req: FindOrCreateUserRequest): UserResponse {

        val existingUser : ExternalAccount? = externalAccountRepository.findByProviderAndProviderUserId(req.provider, req.providerUserId)

        if (existingUser != null) return getById(existingUser.userId)

        val newUser = userRepository.save(
            User(
                email = req.email ?: "",
                firstName = req.firstName ?: "",
                lastName = req.lastName ?: "",
                pfpSrc = req.avatarUrl ?: "",
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

        return userMapper.toUserResponse(newUser, hasOnboarded(newUser.id))
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