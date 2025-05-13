package com.dev.e_shop.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        //product config
        RedisCacheConfiguration productCacheConfiguration = redisCacheConfiguration
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues();

        //cart config
        RedisCacheConfiguration cartCacheConfiguration = redisCacheConfiguration
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues();

        //cart config
        RedisCacheConfiguration profileCacheConfiguration = redisCacheConfiguration
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues();

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .withCacheConfiguration("product", productCacheConfiguration)
                .withCacheConfiguration("products", productCacheConfiguration)
                .withCacheConfiguration("cart", cartCacheConfiguration)
                .withCacheConfiguration("carts", cartCacheConfiguration)
                .withCacheConfiguration("user", profileCacheConfiguration)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
