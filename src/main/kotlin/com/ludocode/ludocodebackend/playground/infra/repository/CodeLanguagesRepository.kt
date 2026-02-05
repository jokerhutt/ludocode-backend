package com.ludocode.ludocodebackend.playground.infra.repository

import com.ludocode.ludocodebackend.playground.domain.entity.CodeLanguages
import org.springframework.data.jpa.repository.JpaRepository
import java.math.BigInteger

interface CodeLanguagesRepository : JpaRepository<CodeLanguages, Long> {

    fun existsByEditorId(editorId: String) : Boolean


}