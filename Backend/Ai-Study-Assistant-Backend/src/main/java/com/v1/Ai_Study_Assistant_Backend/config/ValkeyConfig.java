package com.v1.Ai_Study_Assistant_Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ValkeyConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use Strings for Valkey Keys
        template.setKeySerializer(new StringRedisSerializer());

        // Use the new, non-deprecated JSON serializer for Valkey Values
        template.setValueSerializer(new GenericJacksonJsonRedisSerializer(objectMapper));

        return template;
    }
}
