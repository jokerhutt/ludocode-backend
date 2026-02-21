package com.ludocode.ludocodebackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class LudocodeBackendApplication

fun main(args: Array<String>) {
    runApplication<LudocodeBackendApplication>(*args)
}
