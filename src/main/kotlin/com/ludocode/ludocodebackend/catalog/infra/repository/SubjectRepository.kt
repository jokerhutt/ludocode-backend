package com.ludocode.ludocodebackend.catalog.infra.repository

import com.ludocode.ludocodebackend.catalog.domain.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository

interface SubjectRepository : JpaRepository<Subject, Long> {

    fun existsBySlugOrName(slug: String, name: String): Boolean
    fun findBySlugAndName(slug: String, name: String): Subject?

}