package com.ludocode.ludocodebackend.languages.app.service

import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.api.dto.CreateLanguageRequest
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
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

        assertUnique(req)

        codeLanguagesRepository.save(
            CodeLanguages(
                name = req.name,
                slug = req.slug,
                editorId = req.editorId,
                pistonId = req.pistonId,
                extension = req.extension,
                runtimeVersion = req.runtimeVersion,
                base = req.base,
                iconName = req.iconName,
                initialScript = req.initialScript
            )
        )
        return getAllLanguages()
    }

    private fun assertUnique(req: CreateLanguageRequest) {

        if (codeLanguagesRepository.existsBySlug(req.slug)) {
            throw ApiException(ErrorCode.SLUG_EXISTS)
        }

        if (codeLanguagesRepository.existsByEditorId(req.editorId)) {
            throw ApiException(ErrorCode.EDITOR_ID_EXISTS)
        }

        if (codeLanguagesRepository.existsByPistonId(req.pistonId)) {
            throw ApiException(ErrorCode.PISTON_ID_EXISTS)
        }
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

        if (codeLanguagesRepository.existsByEditorIdAndIdNot(req.editorId, id)) {
            throw ApiException(ErrorCode.EDITOR_ID_EXISTS)
        }

        if (codeLanguagesRepository.existsByPistonIdAndIdNot(req.pistonId, id)) {
            throw ApiException(ErrorCode.PISTON_ID_EXISTS)
        }
    }

}