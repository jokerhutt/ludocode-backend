package com.ludocode.ludocodebackend.commons.configuration.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "yaml.schemas")
class YamlProperties {
    var curriculum: String = "http://localhost:8080"
}