package com.ludocode.ludocodebackend.commons.configuration.web

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class WebConfig(private val corsProperties: CorsProperties) : WebMvcConfigurer {

    override fun addCorsMappings(r: CorsRegistry) {
        r.addMapping("/**")
            .allowedOriginPatterns(*corsProperties.origins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}