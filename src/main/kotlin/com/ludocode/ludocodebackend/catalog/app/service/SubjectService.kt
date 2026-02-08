package com.ludocode.ludocodebackend.catalog.app.service

import com.ludocode.ludocodebackend.catalog.api.dto.request.SubjectRequest
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseSubjectResponse
import com.ludocode.ludocodebackend.catalog.api.dto.snapshot.SubjectSnap
import com.ludocode.ludocodebackend.catalog.app.mapper.CourseMapper
import com.ludocode.ludocodebackend.catalog.domain.entity.Subject
import com.ludocode.ludocodebackend.catalog.infra.repository.CourseRepository
import com.ludocode.ludocodebackend.catalog.infra.repository.SubjectRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import com.ludocode.ludocodebackend.languages.app.LanguagePort
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SubjectService(
    private val subjectRepository: SubjectRepository,
    private val languagePort: LanguagePort,
    private val courseMapper: CourseMapper,
    private val courseRepository: CourseRepository
) {

    @Transactional
    fun createSubject(req: SubjectRequest): List<CourseSubjectResponse> {

        if (subjectRepository.existsBySlug(req.slug)) {
            throw ApiException(ErrorCode.SLUG_EXISTS)
        }

        val languageReference =
            req.codeLanguageId?.let { languageId ->
                languagePort.findById(languageId)
            }

        val subject = Subject(
            name = req.name,
            slug = req.slug,
            codeLanguage = languageReference
        )

        subjectRepository.save(subject)

        return getAllSubjects()
    }

    fun getAllSubjects() : List<CourseSubjectResponse> {
        val subjects = subjectRepository.findAll()
        return courseMapper.toCourseSubjectResponseList(subjects)
    }

    @Transactional
    fun deleteSubject (id: Long) : List<CourseSubjectResponse> {
        if (courseRepository.existsBySubjectId(id)) {
            throw ApiException(ErrorCode.SUBJECT_IN_USE)
        }
        val subject = subjectRepository.findById(id).orElseThrow { ApiException(ErrorCode.SUBJECT_NOT_FOUND) }
        subjectRepository.delete(subject)
        return getAllSubjects()
    }

    @Transactional
    fun updateSubject(id: Long, req: SubjectRequest): List<CourseSubjectResponse> {

        if (subjectRepository.existsBySlugAndIdNot(req.slug, id)) {
            throw ApiException(ErrorCode.SLUG_EXISTS, "This slug already exists on another language")
        }

        val languageReference =
            req.codeLanguageId?.let { languageId ->
                languagePort.findById(languageId)
            }

        val subject = subjectRepository.findById(id).orElseThrow { ApiException(ErrorCode.SUBJECT_NOT_FOUND) }

        subject.slug = req.slug
        subject.name = req.name
        subject.codeLanguage = languageReference

        return getAllSubjects()

    }


}