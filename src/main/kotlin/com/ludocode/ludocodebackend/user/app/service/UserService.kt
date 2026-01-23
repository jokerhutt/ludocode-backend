package com.ludocode.ludocodebackend.user.app.service

import com.ludocode.ludocodebackend.commons.constants.LogEvents
import com.ludocode.ludocodebackend.commons.constants.LogFields
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.user.api.dto.request.FindOrCreateUserRequest
import com.ludocode.ludocodebackend.user.api.dto.request.OnboardingSubmission
import com.ludocode.ludocodebackend.user.api.dto.response.OnboardingResponse
import com.ludocode.ludocodebackend.user.api.dto.response.UserResponse
import com.ludocode.ludocodebackend.user.app.mapper.UserMapper
import com.ludocode.ludocodebackend.progress.app.port.`in`.CourseProgressPortForUser
import com.ludocode.ludocodebackend.user.api.dto.request.EditProfileRequest
import com.ludocode.ludocodebackend.user.api.dto.response.AvatarInfo
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForAuth
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForOnboarding
import com.ludocode.ludocodebackend.user.app.port.`in`.UserPortForProgress
import com.ludocode.ludocodebackend.user.configuration.AvatarConfig
import com.ludocode.ludocodebackend.user.domain.entity.ExternalAccount
import com.ludocode.ludocodebackend.user.domain.entity.User
import com.ludocode.ludocodebackend.user.domain.entity.UserPreferences
import com.ludocode.ludocodebackend.user.infra.repository.ExternalAccountRepository
import com.ludocode.ludocodebackend.onboarding.api.infra.repository.UserPreferencesRepository
import com.ludocode.ludocodebackend.user.infra.repository.UserRepository
import jakarta.transaction.Transactional
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
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
) : UserPortForProgress, UserPortForAuth, UserPortForOnboarding {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

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
        logger.warn(LogEvents.USER_DELETED)
        existingUser.isDeleted = true
    }

    @Transactional
    override fun setDisplayName (userId: UUID, displayName: String) {
        val user = userRepository.findById(userId).orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        user.displayName = displayName
        userRepository.save(user)
    }

    @Transactional
    override fun findOrCreate(req: FindOrCreateUserRequest): UserResponse {

        val existingUser : ExternalAccount? = externalAccountRepository.findByProviderAndProviderUserId(req.provider, req.providerUserId)

        if (existingUser != null)  {
            logger.info(LogEvents.USER_LOGIN_EXISTING + " {}", kv(LogFields.PROVIDER, req.provider.name))
            return getById(existingUser.userId)
        }

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

        logger.info(
            LogEvents.USER_CREATED + " {} {}",
            kv(LogFields.PROVIDER, req.provider.name),
            kv(LogFields.PROVIDER_USER_ID, req.providerUserId)
        )

        return userMapper.toUserResponse(newUser, hasOnboarded(newUser))
    }

    private fun assignAvatar (): AvatarInfo{
        val index = Random.nextInt(1, avatarConfig.count + 1)
        val version = avatarConfig.version
        return AvatarInfo(version, index)
    }

    @Transactional
    internal fun editUser(userId: UUID, request: EditProfileRequest) : UserResponse {
        val user = userRepository.findById(userId).orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        if ((user.displayName != request.username) && request.username.isNotEmpty()) {
            val oldName = user.displayName
            user.displayName = request.username
            logger.info(
                LogEvents.USERNAME_CHANGED + " {} {}",
                kv(LogFields.OLD_USERNAME, oldName ?: "null"),
                kv(LogFields.NEW_USERNAME, request.username)
            )
        }
        userRepository.save(user)

        val res = changeUserAvatar(user, request.avatarInfo)
        return res
    }

    @Transactional
    internal fun changeUserAvatar(userId: UUID, avatarInfo: AvatarInfo): UserResponse {
        val user = userRepository.findById(userId).orElseThrow { ApiException(ErrorCode.USER_NOT_FOUND) }
        return changeUserAvatar(user, avatarInfo)
    }


    @Transactional
    internal fun changeUserAvatar(user: User, avatarInfo: AvatarInfo): UserResponse{

        val newIndex = avatarInfo.index
        val newVersion = avatarInfo.version

        val oldIndex = user.avatarIndex
        val oldVersion = user.avatarVersion

        val hasUserOnboarded = hasOnboarded(user)

        val validIndexes = avatarConfig.count
        if (newIndex < 1 || newIndex > validIndexes) throw ApiException(ErrorCode.BAD_REQ, "This avatar does not exist")

        if (user.avatarIndex == newIndex && user.avatarVersion == newVersion) {
            return userMapper.toUserResponse(user, hasUserOnboarded)
        }

        user.avatarVersion = newVersion
        user.avatarIndex = newIndex
        userRepository.save(user)

        logger.info(
            LogEvents.USER_AVATAR_CHANGED + " {} {} {} {}",
            kv(LogFields.OLD_AVATAR_INDEX, oldIndex),
            kv(LogFields.NEW_AVATAR_INDEX, newIndex),
            kv(LogFields.OLD_AVATAR_VERSION, oldVersion),
            kv(LogFields.NEW_AVATAR_VERSION, newVersion),
        )

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

    internal fun getUsersByIds(userIds: List<UUID>): List<UserResponse> {
        val users = userRepository.findAllByIdIn(userIds)
        val onboardingMap = users.associate { it.id to hasOnboarded(it) }
        return userMapper.toUserResponseList(users, onboardingMap)
    }

}