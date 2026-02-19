package com.ludocode.ludocodebackend

import com.ludocode.ludocodebackend.config.FirebaseAuthTestConfig
import com.ludocode.ludocodebackend.config.GcpTestConfig
import com.ludocode.ludocodebackend.config.GeminiTestConfig
import com.ludocode.ludocodebackend.config.PistonTestConfig
import com.ludocode.ludocodebackend.config.security.TestSecurityConfig
import com.ludocode.ludocodebackend.config.time.TestClockConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(
    TestcontainersConfiguration::class,
    GcpTestConfig::class,
    GeminiTestConfig::class,
    FirebaseAuthTestConfig::class,
    GcpTestConfig::class,
    PistonTestConfig::class,
    TestSecurityConfig::class,
    TestClockConfig::class
)
@SpringBootTest
@ActiveProfiles("test")
class LudocodeBackendApplicationTests {

    @Test
    fun contextLoads() {

    }

}
