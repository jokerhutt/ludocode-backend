package com.ludocode.ludocodebackend.commons.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProps::class)
class WebConfig(private val corsProps: CorsProps) : WebMvcConfigurer {

    override fun addCorsMappings(r: CorsRegistry) {
        r.addMapping("/**")
            .allowedOriginPatterns(*corsProps.origins.toTypedArray())
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}