package com.ludocode.ludocodebackend.support

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer

object Containers {

    @JvmStatic
    val POSTGRES = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("ludocode")
        withUsername("postgres")
        withPassword("password")
        start()
    }

    @JvmStatic
    val FAKE_GCS = GenericContainer<Nothing>("fsouza/fake-gcs-server:latest").apply {
        withExposedPorts(4443)
        withCommand("-scheme", "http", "-port", "4443")
        start()
    }

}