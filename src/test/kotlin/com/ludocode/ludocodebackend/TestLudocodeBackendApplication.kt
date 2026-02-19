package com.ludocode.ludocodebackend

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<LudocodeBackendApplication>().with(TestcontainersConfiguration::class).run(*args)
}
