package com.ludocode.ludocodebackend

import com.ludocode.ludocodebackend.config.FixedClockConfig
import com.ludocode.ludocodebackend.config.GcpTestConfig
import com.ludocode.ludocodebackend.config.GeminiTestConfig
import com.ludocode.ludocodebackend.config.GoogleOAuthTestConfig
import com.ludocode.ludocodebackend.config.PistonTestConfig
import com.ludocode.ludocodebackend.config.security.TestSecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(TestcontainersConfiguration::class, GcpTestConfig::class, GeminiTestConfig::class, GoogleOAuthTestConfig::class, GcpTestConfig::class,
	PistonTestConfig::class, TestSecurityConfig::class, FixedClockConfig::class)
@SpringBootTest
@ActiveProfiles("test")
class LudocodeBackendApplicationTests {

	@Test
	fun contextLoads() {

	}

}
