package com.pjieyi.aianswer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 *redis  session序列化器
 * 如果不配置这个就需要使用RedisIndexedSessionRepository 来获取session登录信息
 */
@Configuration
public class RedisSessionRepositoryConfig {

    @Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> getRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

}
