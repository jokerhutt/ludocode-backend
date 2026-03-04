package com.ludocode.ludocodebackend.commons.configuration.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class YamlConfig : WebMvcConfigurer {

    @Bean
    fun yamlMapper(): ObjectMapper =
        ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule.Builder().build())

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val mapper = ObjectMapper(YAMLFactory())
            .registerModule(KotlinModule.Builder().build())

        val converter = MappingJackson2HttpMessageConverter(mapper)
        converter.supportedMediaTypes = listOf(
            MediaType("application", "x-yaml"),
            MediaType("application", "yaml"),
            MediaType("text", "yaml")
        )

        converters.add(converter)
    }
}