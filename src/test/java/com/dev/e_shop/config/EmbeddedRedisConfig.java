package com.dev.e_shop.config;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

@TestConfiguration
@Profile("test")
public class EmbeddedRedisConfig {
    private  RedisServer redisServer;
    private int port;

    @Bean
    public RedisServer redisProperties() throws IOException {
        port = findAvailablePort();
        redisServer = new RedisServer(port);
        redisServer.start();

        return redisServer;
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to find available port for Redis", e);
        }
    }

    @Bean
    @Primary
    public LettuceConnectionFactory embeddedRedisConnectionFactory() {
        return new LettuceConnectionFactory(
                "localhost", port);
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(LettuceConnectionFactory embeddedRedisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(embeddedRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

}

