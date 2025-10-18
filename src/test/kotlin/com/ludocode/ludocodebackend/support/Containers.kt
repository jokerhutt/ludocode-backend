package com.ludocode.ludocodebackend.support

import org.testcontainers.containers.PostgreSQLContainer

object Containers {

    @JvmStatic
    val POSTGRES = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("ludocode")
        withUsername("postgres")
        withPassword("password")
        start()
    }
}