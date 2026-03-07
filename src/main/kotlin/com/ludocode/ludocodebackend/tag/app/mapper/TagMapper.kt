package com.ludocode.ludocodebackend.tag.app.mapper

import com.ludocode.ludocodebackend.commons.mapper.BasicMapper
import com.ludocode.ludocodebackend.tag.api.dto.TagMetadata
import com.ludocode.ludocodebackend.tag.domain.entity.Tag
import org.springframework.stereotype.Component

@Component
class TagMapper (private val basicMapper: BasicMapper) {

    fun toTagMetadata(tag: Tag) : TagMetadata =
        basicMapper.one(tag) {
            TagMetadata(
                id = it.id,
                name = it.name,
                slug = it.slug
            )
        }

    fun toTagMetadataList(tags: List<Tag>): List<TagMetadata> =
        basicMapper.list(tags) { subject ->
            toTagMetadata(subject)
        }

}