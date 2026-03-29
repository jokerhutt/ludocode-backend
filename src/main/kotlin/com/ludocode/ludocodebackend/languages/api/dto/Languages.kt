package com.ludocode.ludocodebackend.languages.api.dto

object Languages {

    val JavaScript = LanguageMetadata("javascript", ".js")
    val Python     = LanguageMetadata("python", ".py")
    val HTML       = LanguageMetadata("html", ".html")
    val CSS        = LanguageMetadata("css", ".css")
    val Lua        = LanguageMetadata("lua", ".lua")

    private val all = listOf(JavaScript, Python, HTML, CSS, Lua)

    val byName = all.associateBy { it.name }

    fun validatePath(name: String, path: String): LanguageMetadata {
        val lang = byName[name.lowercase()]
            ?: throw IllegalArgumentException("Invalid language: $name")

        val ext = extractExtension(path)
            ?: throw IllegalArgumentException("No file extension in path")

        if (ext != lang.extension) {
            throw IllegalArgumentException("Invalid file extension for $name: $ext")
        }

        return lang
    }

    private fun extractExtension(path: String): String? {
        val fileName = path.substringAfterLast('/')
        val dotIndex = fileName.lastIndexOf('.')

        if (dotIndex == -1 || dotIndex == fileName.length - 1) return null

        return fileName.substring(dotIndex).lowercase()
    }

}