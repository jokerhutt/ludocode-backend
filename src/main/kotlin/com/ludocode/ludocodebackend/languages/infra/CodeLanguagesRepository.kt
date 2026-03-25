package com.ludocode.ludocodebackend.languages.infra

import com.ludocode.ludocodebackend.languages.entity.CodeLanguages
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface CodeLanguagesRepository : JpaRepository<CodeLanguages, Long> {

    @Query("select l.id from CodeLanguages l")
    fun findAllIds(): List<UUID>

    fun existsBySlug(slug: String): Boolean

    fun existsBySlugAndIdNot(slug: String, id: Long): Boolean


}