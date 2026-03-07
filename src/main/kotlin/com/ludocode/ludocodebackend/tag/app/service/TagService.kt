package com.ludocode.ludocodebackend.tag.app.service

import com.ludocode.ludocodebackend.tag.api.dto.TagMetadata
import com.ludocode.ludocodebackend.tag.app.mapper.TagMapper
import com.ludocode.ludocodebackend.tag.domain.entity.Tag
import com.ludocode.ludocodebackend.tag.infra.repository.TagRepository
import com.ludocode.ludocodebackend.commons.exception.ApiException
import com.ludocode.ludocodebackend.commons.exception.ErrorCode
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class TagService(
    private val tagRepository: TagRepository,
    private val  tagMapper: TagMapper,
) {

    @Transactional
    fun createTag(req: TagMetadata): List<TagMetadata> {

        if (tagRepository.existsBySlug(req.slug)) {
            throw ApiException(ErrorCode.SLUG_EXISTS)
        }

        val tag = Tag(
            name = req.name,
            slug = req.slug,
        )

        tagRepository.save(tag)

        return getAllTags()
    }

    fun getAllTags(): List<TagMetadata> {
        val tags = tagRepository.findAll()
        return tagMapper.toTagMetadataList(tags)
    }

    @Transactional
    fun deleteTag(id: Long): List<TagMetadata> {
        val tag = tagRepository.findById(id).orElseThrow { ApiException(ErrorCode.TAG_NOT_FOUND) }
        tagRepository.delete(tag)
        return getAllTags()
    }

    @Transactional
    fun updateTag(id: Long, req: TagMetadata): List<TagMetadata> {

        if (tagRepository.existsBySlugAndIdNot(req.slug, id)) {
            throw ApiException(ErrorCode.SLUG_EXISTS, "This slug already exists on another tag")
        }

        val tag = tagRepository.findById(id).orElseThrow { ApiException(ErrorCode.TAG_NOT_FOUND) }

        tag.slug = req.slug
        tag.name = req.name

        return getAllTags()

    }


}