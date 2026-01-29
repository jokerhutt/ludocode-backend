package com.ludocode.ludocodebackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
class LudocodeBackendApplication

fun main(args: Array<String>) {
	runApplication<LudocodeBackendApplication>(*args)
}
