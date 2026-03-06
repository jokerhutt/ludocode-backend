package com.ludocode.ludocodebackend.tag.infra.repository

import com.ludocode.ludocodebackend.tag.domain.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectRepository : JpaRepository<Subject, Long> {

    fun existsBySlugOrName(slug: String, name: String): Boolean
    fun findBySlugAndName(slug: String, name: String): Subject?

    fun existsBySlug(slug: String): Boolean
    fun existsBySlugAndIdNot(slug: String, id: Long): Boolean

}