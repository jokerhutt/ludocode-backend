package com.ludocode.ludocodebackend.config

import com.google.cloud.NoCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.ludocode.ludocodebackend.support.Containers
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@TestConfiguration
class GcpTestConfig {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerFakeGcs(reg: DynamicPropertyRegistry) {
            val gcs = Containers.FAKE_GCS
            reg.add("app.gcs.host") { "http://${gcs.host}:${gcs.getMappedPort(4443)}" }
            reg.add("gcs.project.id") { "test-project" }
            reg.add("gcs.bucket") { "test-bucket" }
        }
    }

    @Bean
    fun testStorage(): Storage {
        val gcs = Containers.FAKE_GCS
        val host = "http://${gcs.host}:${gcs.getMappedPort(4443)}"

        return StorageOptions.newBuilder()
            .setProjectId("test-project")
            .setHost(host)
            .setCredentials(NoCredentials.getInstance())
            .build()
            .service
    }
}