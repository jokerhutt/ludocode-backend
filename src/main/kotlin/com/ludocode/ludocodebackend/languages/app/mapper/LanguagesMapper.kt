package com.ludocode.ludocodebackend.languages.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.languages.api.dto.LanguageMetadata
import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import org.springframework.stereotype.Component

@Component
class LanguagesMapper(private val basicMapper: BasicMapper) {

    fun toLanguageMetadata(language: CodeLanguages): LanguageMetadata {
        return basicMapper.one(language) {
            LanguageMetadata(
                name = it.name,
                languageId = it.id,
                editorId = it.editorId,
                initialScript = it.initialScript ?: "",
                slug = it.slug,
                pistonId = it.pistonId,
                extension = it.extension,
                base = it.base,
                runtimeVersion = it.runtimeVersion,
                iconName = it.iconName,
                enabled = it.isEnabled,
                disabledReason = it.disabledReason
            )
        }
    }

    fun toLanguageMetadataList(languages: List<CodeLanguages>): List<LanguageMetadata> {
        return basicMapper.list(languages) { it -> toLanguageMetadata(it) }
    }

}