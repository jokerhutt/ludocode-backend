package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.request.SubjectRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.SubjectMetadata
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.app.mapper.SubjectMapper
import com.ludocode.ludocodebackend.catalog.domain.entity.Subject
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.SubjectRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.app.LanguagePort
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubjectService(
    private val subjectRepository: SubjectRepository,
    private val  subjectMapper: SubjectMapper,
    private val courseRepository: CourseRepository
) {

    @Transactional
    fun createSubject(req: SubjectMetadata): List<SubjectMetadata> {

        if (subjectRepository.existsBySlug(req.slug)) {
            throw ApiException(ErrorCode.SLUG_EXISTS)
        }

        val subject = Subject(
            name = req.name,
            slug = req.slug,
        )

        subjectRepository.save(subject)

        return getAllSubjects()
    }

    fun getAllSubjects(): List<SubjectMetadata> {
        val subjects = subjectRepository.findAll()
        return subjectMapper.toSubjectMetadataList(subjects)
    }

    @Transactional
    fun deleteSubject(id: Long): List<SubjectMetadata> {
        if (courseRepository.existsBySubjectId(id)) {
            throw ApiException(ErrorCode.SUBJECT_IN_USE)
        }
        val subject = subjectRepository.findById(id).orElseThrow { ApiException(ErrorCode.SUBJECT_NOT_FOUND) }
        subjectRepository.delete(subject)
        return getAllSubjects()
    }

    @Transactional
    fun updateSubject(id: Long, req: SubjectMetadata): List<SubjectMetadata> {

        if (subjectRepository.existsBySlugAndIdNot(req.slug, id)) {
            throw ApiException(ErrorCode.SLUG_EXISTS, "This slug already exists on another language")
        }

        val subject = subjectRepository.findById(id).orElseThrow { ApiException(ErrorCode.SUBJECT_NOT_FOUND) }

        subject.slug = req.slug
        subject.name = req.name

        return getAllSubjects()

    }


}