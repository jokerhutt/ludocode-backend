package com.ludocode.ludocodebackend.catalog.app.mapper

import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.SubjectMetadata
import com.ludocode.ludocodebackend.tag.domain.entity.Subject
import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import org.springframework.stereotype.Component

@Component
class SubjectMapper (private val basicMapper: BasicMapper) {

    fun toSubjectMetadata(subject: Subject) : SubjectMetadata =
        basicMapper.one(subject) {
            SubjectMetadata(
                id = it.id,
                name = it.name,
                slug = it.slug
            )
        }

    fun toSubjectMetadataList(subjects: List<Subject>): List<SubjectMetadata> =
        basicMapper.list(subjects) {subject ->
            toSubjectMetadata(subject)
        }

}