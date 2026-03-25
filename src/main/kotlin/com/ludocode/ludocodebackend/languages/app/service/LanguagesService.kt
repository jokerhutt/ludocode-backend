package com.ludocode.ludocodebackend.languages.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.api.dto.CreateLanguageRequest
import com.ludocode.ludocodebackend.languages.api.dto.LanguageDisabledMessageRequest
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.languages.api.dto.LanguageToggleRequest
import com.ludocode.ludocodebackend.languages.api.dto.UpdateLanguageRequest
import com.ludocode.ludocodebackend.languages.app.LanguagePort
import com.ludocode.ludocodebackend.languages.app.mapper.LanguagesMapper
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import com.ludocode.ludocodebackend.languages.infra.CodeLanguagesRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class LanguagesService(
    private val codeLanguagesRepository: CodeLanguagesRepository,
    private val languagesMapper: LanguagesMapper
) : LanguagePort {

    override fun findById(languageId: Long): CodeLanguages {
        return codeLanguagesRepository
            .findById(languageId)
            .orElseThrow { ApiException(ErrorCode.LANGUAGE_NOT_FOUND) }
    }

    internal fun getAllLanguages(): List<LanguageMetadata> {
        val languages = codeLanguagesRepository.findAll()
        return languagesMapper.toLanguageMetadataList(languages)
    }

    @Transactional
    internal fun createLanguage(req: CreateLanguageRequest): List<LanguageMetadata> {

        if (codeLanguagesRepository.existsBySlug(req.slug)) {
            throw ApiException(ErrorCode.SLUG_EXISTS)
        }

        codeLanguagesRepository.save(
            CodeLanguages(
                name = req.name,
                slug = req.slug,
                editorId = req.editorId,
                pistonId = req.pistonId,
                extension = req.extension,
                runtimeVersion = req.runtimeVersion,
                runtime = req.runtime,
                base = req.base,
                iconName = req.iconName,
                initialScript = req.initialScript,
                isEnabled = true,
                disabledReason = null
            )
        )
        return getAllLanguages()
    }

    @Transactional
    internal fun toggleLanguage(languageId: Long, req: LanguageToggleRequest): List<LanguageMetadata> {
        val language = codeLanguagesRepository.findById(languageId)
            .orElseThrow { ApiException(ErrorCode.LANGUAGE_NOT_FOUND) }

        language.isEnabled = req.enabled

        if (req.enabled) {
            language.disabledReason = null
        } else {
            val disabledMessage = req.message?.trim()
            if (disabledMessage.isNullOrBlank()) {
                throw ApiException(ErrorCode.BAD_REQ, "Disabled message is required when disabling a language")
            }
            language.disabledReason = disabledMessage
        }

        return getAllLanguages()
    }

    @Transactional
    internal fun updateDisabledMessage(languageId: Long, req: LanguageDisabledMessageRequest): List<LanguageMetadata> {
        val language = codeLanguagesRepository.findById(languageId)
            .orElseThrow { ApiException(ErrorCode.LANGUAGE_NOT_FOUND) }

        if (language.isEnabled) {
            throw ApiException(ErrorCode.BAD_REQ, "Can not set disabled message while language is enabled")
        }

        val message = req.message.trim()
        if (message.isBlank()) {
            throw ApiException(ErrorCode.BAD_REQ, "Disabled message can not be blank")
        }

        language.disabledReason = message
        return getAllLanguages()
    }

    @Transactional
    internal fun updateLanguage(id: Long, req: UpdateLanguageRequest): List<LanguageMetadata> {
        assertUniqueForUpdate(id, req)
        val language = codeLanguagesRepository.findById(id)
            .orElseThrow { ApiException(ErrorCode.LANGUAGE_NOT_FOUND) }
        language.name = req.name
        language.slug = req.slug
        language.base = req.base
        language.iconName = req.iconName
        language.runtimeVersion = req.runtimeVersion
        language.runtime = req.runtime
        language.extension = req.extension
        language.initialScript = req.initialScript
        language.pistonId = req.pistonId
        language.editorId = req.editorId

        return getAllLanguages()
    }


    @Transactional
    internal fun deleteLanguage(id: Long): List<LanguageMetadata> {
        val existingLanguage = codeLanguagesRepository.findById(id)
            .orElseThrow {
                ApiException(
                    ErrorCode.LANGUAGE_NOT_FOUND,
                    "The language doesn't exist. Have you already deleted it?"
                )
            }
        codeLanguagesRepository.delete(existingLanguage)
        return getAllLanguages()
    }

    private fun assertUniqueForUpdate(id: Long, req: UpdateLanguageRequest) {

        if (codeLanguagesRepository.existsBySlugAndIdNot(req.slug, id)) {
            throw ApiException(ErrorCode.SLUG_EXISTS)
        }
    }

}