package com.ludocode.ludocodebackend.commons.configuration

import com.google.protobuf.Api
import com.ludocode.ludocodebackend.auth.api.security.JwtCookieAuthenticationFilter
import com.ludocode.ludocodebackend.commons.constants.ApiPaths
import com.ludocode.ludocodebackend.commons.constants.AuthRoleEndpointConstants
import com.ludocode.ludocodebackend.commons.constants.PublicEndpointConstants
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@Profile("!test")
class SecurityConfig(
    private val jwtFilter: JwtCookieAuthenticationFilter,
    private val corsLoggingFilter: CorsLoggingFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                it.requestMatchers(*PublicEndpointConstants.PUBLIC_ENDPOINTS).permitAll()
                it.requestMatchers("${ApiPaths.USERS.BASE}/**").authenticated()
                it.requestMatchers(*AuthRoleEndpointConstants.ADMIN_REQUIRED)
                    .hasRole("ADMIN")
                it.requestMatchers("/media/**").permitAll()
                it.requestMatchers("/internal/**", "/internal/v1/**").permitAll()
                it.requestMatchers("/actuator/**").permitAll()
                it.requestMatchers("/api/v1/auth/me").authenticated()
                it.anyRequest().authenticated()
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .addFilterBefore(corsLoggingFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}