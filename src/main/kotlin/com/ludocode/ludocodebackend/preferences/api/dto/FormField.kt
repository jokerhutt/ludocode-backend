package com.ludocode.ludocodebackend.preferences.api.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SelectFieldDef::class, name = "select"),
    JsonSubTypes.Type(value = TextFieldDef::class, name = "text"),
    JsonSubTypes.Type(value = BooleanFieldDef::class, name = "boolean"),
)
sealed interface FormFieldDef {
    val key: String
    val label: String
    val required: Boolean
}

data class SelectFieldDef(
    override val key: String,
    override val label: String,
    override val required: Boolean = true,
    val options: List<OptionDef>,
) : FormFieldDef

data class TextFieldDef(
    override val key: String,
    override val label: String,
    override val required: Boolean = true,
    val minLen: Int? = null,
    val maxLen: Int? = null,
    val regex: String? = null,
) : FormFieldDef

data class BooleanFieldDef(
    override val key: String,
    override val label: String,
    override val required: Boolean = true,
) : FormFieldDef

data class OptionDef(
    val value: String,
    val label: String,
)