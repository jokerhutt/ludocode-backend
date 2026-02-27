package com.ludocode.ludocodebackend.lesson.domain.jsonb

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = HeaderBlock::class, name = "header"),
    JsonSubTypes.Type(value = ParagraphBlock::class, name = "paragraph"),
    JsonSubTypes.Type(value = CodeBlock::class, name = "code"),
    JsonSubTypes.Type(value = MediaBlock::class, name = "media")
)
sealed interface Block
data class HeaderBlock(val content: String) : Block
data class ParagraphBlock(val content: String) : Block
data class CodeBlock(val language: String, val content: String) : Block
data class MediaBlock(val src: String, val alt: String? = null) : Block