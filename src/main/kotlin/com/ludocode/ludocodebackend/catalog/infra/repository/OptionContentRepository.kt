package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.OptionContent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface OptionContentRepository : JpaRepository<OptionContent, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO option_content (content)
        VALUES (:content)
        ON CONFLICT (content) DO NOTHING
        """, nativeQuery = true
    )
    fun upsertOption(@Param("content") content: String)

    @Query(value = """
        SELECT *
        FROM option_content
        WHERE option_content.content = :content
        """, nativeQuery = true)
    fun findOptionContentByContent(@Param("content") content: String) : OptionContent?

}