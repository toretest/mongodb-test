package net.toregard.mongodbtest.domains

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * JacksonConfig class provides a configuration for Jackson ObjectMapper.
 * This configuration ensures that the ObjectMapper supports Kotlin module features.
 *
 * @Configuration: Indicates that this class contains one or more bean methods
 *                 annotated with @Bean producing beans manageable by the Spring container.
 */
@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerKotlinModule()
    }
}
