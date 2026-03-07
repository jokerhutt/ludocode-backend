package com.ludocode.ludocodebackend.tag.infra.repository

import com.ludocode.ludocodebackend.tag.domain.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {

    fun existsBySlugOrName(slug: String, name: String): Boolean
    fun findBySlugAndName(slug: String, name: String): Tag?

    fun existsBySlug(slug: String): Boolean
    fun existsBySlugAndIdNot(slug: String, id: Long): Boolean

    fun findByIdIn(ids: List<Long>): List<Tag>

}