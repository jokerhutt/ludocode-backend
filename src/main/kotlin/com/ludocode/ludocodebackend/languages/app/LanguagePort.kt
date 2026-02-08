package com.ludocode.ludocodebackend.languages.app

import com.ludocode.ludocodebackend.languages.entity.CodeLanguages

interface LanguagePort {
    fun findById(languageId: Long): CodeLanguages
}