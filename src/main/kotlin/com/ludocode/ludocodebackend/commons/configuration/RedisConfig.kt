package com.ludocode.ludocodebackend.commons.configuration
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ludocode.ludocodebackend.catalog.api.dto.internal.LessonTreeWithIdDTO
import com.ludocode.ludocodebackend.catalog.api.dto.response.CourseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.ExerciseResponse
import com.ludocode.ludocodebackend.catalog.api.dto.response.tree.FlatCourseTreeResponse
import com.ludocode.ludocodebackend.commons.constants.CacheNames
import io.lettuce.core.ClientOptions
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
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
@EnableConfigurationProperties(RedisProps::class)
@Profile("!test")
class RedisConfig (private val redisProps: RedisProps) {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper =
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .findAndRegisterModules()

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisProps.host, redisProps.port)
        if (redisProps.password.isNotBlank()) {
            config.setPassword(redisProps.password)
        }

        val clientConfig = LettuceClientConfiguration.builder()
            .clientOptions(ClientOptions.builder().build())
            .build()

        return LettuceConnectionFactory(config, clientConfig)
    }

    @Bean
    fun cacheManager(
        connectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): CacheManager {

        return RedisCacheManager.builder(connectionFactory)
            .withCacheConfiguration(
                CacheNames.COURSE_TREE,
                valueCache(objectMapper, FlatCourseTreeResponse::class.java, Duration.ofMinutes(30))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_TREE,
                valueCache(objectMapper, LessonTreeWithIdDTO::class.java, Duration.ofMinutes(30))
            )
            .withCacheConfiguration(
                CacheNames.COURSE_LIST,
                listCache(objectMapper, CourseResponse::class.java, Duration.ofMinutes(10))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_EXERCISES,
                listCache(objectMapper, ExerciseResponse::class.java, Duration.ofMinutes(10))
            )
            .withCacheConfiguration(
                CacheNames.COURSE_FIRST_LESSON,
                valueCache(objectMapper, UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_MODULE,
                valueCache(objectMapper, UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_NEXT,
                valueCache(objectMapper, UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.LESSON_COURSE,
                valueCache(objectMapper, UUID::class.java, Duration.ofMinutes(60))
            )
            .withCacheConfiguration(
                CacheNames.EXERCISE_SINGLE,
                valueCache(objectMapper, ExerciseResponse::class.java, Duration.ofMinutes(30))
            )
            .build()
    }

    private fun <T> valueCache(
        objectMapper: ObjectMapper,
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

    private fun <T> listCache(
        objectMapper: ObjectMapper,
        elementClass: Class<T>,
        ttl: Duration
    ): RedisCacheConfiguration {

        val javaType = objectMapper.typeFactory
            .constructCollectionType(List::class.java, elementClass)

        val serializer = Jackson2JsonRedisSerializer<Any>(objectMapper, javaType)

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            )
    }
}