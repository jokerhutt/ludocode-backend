package com.ludocode.ludocodebackend.commons.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import io.lettuce.core.ClientOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration
import java.util.UUID

@Configuration
@EnableCaching
class RedisConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port}")
    private var redisPort: Int = 6379

    @Value("\${spring.data.redis.password:}")
    private var redisPassword: String = ""

    // ---------- ObjectMapper (NORMAL, no typing) ----------
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .findAndRegisterModules()

    // ---------- Redis connection ----------
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        if (redisPassword.isNotBlank()) {
            config.setPassword(redisPassword)
        }

        val clientConfig = LettuceClientConfiguration.builder()
            .clientOptions(ClientOptions.builder().build())
            .build()

        return LettuceConnectionFactory(config, clientConfig)
    }

    // ---------- CacheManager (TYPED serializers) ----------
    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): CacheManager {

        fun <T> typedCache(
            clazz: Class<T>,
            ttl: Duration
        ): RedisCacheConfiguration =
            RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        StringRedisSerializer()
                    )
                )
                .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(
                        Jackson2JsonRedisSerializer(objectMapper, clazz)
                    )
                )

        return RedisCacheManager.builder(connectionFactory)
            .withCacheConfiguration(
                CacheNames.COURSE_TREE,
                typedCache(FlatCourseTreeResponse::class.java, Duration.ofMinutes(30))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_TREE,
                typedCache(LessonTreeWithIdDTO::class.java, Duration.ofMinutes(30))
            )
            .withCacheConfiguration(
                CacheNames.COURSE_LIST,
                typedCache(
                    objectMapper.typeFactory
                        .constructCollectionType(
                            List::class.java,
                            CourseResponse::class.java
                        )
                        .rawClass,
                    Duration.ofMinutes(10)
                )
            )
            .withCacheConfiguration(
                CacheNames.LESSON_EXERCISES,
                typedCache(
                    objectMapper.typeFactory
                        .constructCollectionType(
                            List::class.java,
                            ExerciseResponse::class.java
                        )
                        .rawClass,
                    Duration.ofMinutes(10)
                )
            )
            .withCacheConfiguration(
                CacheNames.COURSE_FIRST_LESSON,
                typedCache(UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_MODULE,
                typedCache(UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_NEXT,
                typedCache(UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_COURSE,
                typedCache(UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.EXERCISE_SINGLE,
                typedCache(ExerciseResponse::class.java, Duration.ofMinutes(30))
            )
            .build()
    }
}