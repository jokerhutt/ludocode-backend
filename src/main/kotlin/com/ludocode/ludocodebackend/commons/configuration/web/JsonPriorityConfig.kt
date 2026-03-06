package com.ludocode.ludocodebackend.commons.configuration.web

import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class JsonPriorityConfig : WebMvcConfigurer {

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val jsonConverters = converters.filterIsInstance<MappingJackson2HttpMessageConverter>()
        converters.removeAll(jsonConverters)
        converters.addAll(0, jsonConverters)
    }
}