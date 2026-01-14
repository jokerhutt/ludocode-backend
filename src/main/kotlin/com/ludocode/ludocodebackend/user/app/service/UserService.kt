package com.ludocode.ludocodebackend.user.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.mapper.UserMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.user.api.dto.response.AvatarInfo
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForAuth
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForProgress
import com.ludocode.ludocodebackend.user.configuration.AvatarConfig
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
import kotlin.random.Random

@Service
class UserService(
    private val userRepository: UserRepository,
    private val externalAccountRepository: ExternalAccountRepository,
    private val userMapper: UserMapper,
    private val clock: Clock,
    private val avatarConfig: AvatarConfig,
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
    internal fun deleteUser(userId: UUID) {
        var existingUser = userRepository.findById(userId).orElseThrow()
        val userExternalAccount = externalAccountRepository.findByUserId(userId) ?: throw ApiException(ErrorCode.USER_NOT_FOUND, "Could not find external account for user")
        externalAccountRepository.delete(userExternalAccount)
        existingUser.isDeleted = true
    }

    @Transactional
    override fun findOrCreate(req: FindOrCreateUserRequest): UserResponse {

        val existingUser : ExternalAccount? = externalAccountRepository.findByProviderAndProviderUserId(req.provider, req.providerUserId)

        if (existingUser != null) return getById(existingUser.userId)

        val assignedAvatar = assignAvatar()

        var newUser = userRepository.save(
            User(
                email = req.email ?: "",
                displayName = req.displayName,
                avatarIndex = assignedAvatar.index,
                avatarVersion = assignedAvatar.version,
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
        return userMapper.toUserResponse(newUser, hasOnboarded(newUser))
    }

    private fun assignAvatar (): AvatarInfo{
        val index = Random.nextInt(1, avatarConfig.count + 1)
        val version = avatarConfig.version
        return AvatarInfo(version, index)
    }

    @Transactional
    internal fun changeUserAvatar(userId: UUID, avatarInfo: AvatarInfo): UserResponse{
        val user = userRepository.findById(userId)
            .orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        val selectedAvatarIndex = avatarInfo.index
        val selectedAvatarVersion = avatarInfo.version
        val hasUserOnboarded = hasOnboarded(user)

        val validIndexes = avatarConfig.count
        if (selectedAvatarIndex < 1 || selectedAvatarIndex > validIndexes) throw ApiException(ErrorCode.BAD_REQ, "This avatar does not exist")

        if (user.avatarIndex == selectedAvatarIndex && user.avatarVersion == selectedAvatarVersion) {
            return userMapper.toUserResponse(user, hasUserOnboarded)
        }

        user.avatarVersion = selectedAvatarVersion
        user.avatarIndex = selectedAvatarIndex
        userRepository.save(user)
        return userMapper.toUserResponse(user, hasUserOnboarded)

    }


    private fun hasOnboarded(userId: UUID): Boolean {
        val user = userRepository.findById(userId).orElseThrow()
        return hasOnboarded(user)
    }

    private fun hasOnboarded (user: User) : Boolean {
        val displayNameExists = user.displayName != null
        val preferencesExist = userPreferencesRepository.existsById(user.id)
        val currentCourseExists = courseProgressPortForUser.existsAnyByUserId(user.id)
        return preferencesExist && currentCourseExists && displayNameExists
    }

    @Transactional
    internal fun createPreferences (submission: OnboardingSubmission, userId: UUID) : OnboardingResponse {
        val toSubmit = UserPreferences(userId = userId, hasExperience = submission.hasProgrammingExperience, chosenPath = submission.chosenPath)

        val selectedUser = userRepository.findById(userId).orElseThrow()
        selectedUser.displayName = submission.selectedUsername
        userRepository.save(selectedUser)

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
        val onboardingMap = users.associate { it.id to hasOnboarded(it) }
        return userMapper.toUserResponseList(users, onboardingMap)
    }

}