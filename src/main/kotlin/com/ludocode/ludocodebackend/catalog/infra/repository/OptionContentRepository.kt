package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.OptionContent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface OptionContentRepository : JpaRepository<OptionContent, UUID> {

    @Modifying
    @Query(
        value = """
      INSERT INTO option_content (id, content)
      VALUES (:id, :content)
      ON CONFLICT (content) DO NOTHING
    """,
        nativeQuery = true
    )
    fun upsertOption(@Param("id") id: UUID, @Param("content") content: String)

    fun findAllByContentIn(contents: Collection<String>): List<OptionContent>


    fun findByContent(content: String): OptionContent?

}