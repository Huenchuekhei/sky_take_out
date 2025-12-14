package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
@Slf4j
public class RedisConfiguration {
    public RedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        /*Spring Data Redis中提供了一个高度封装的类：RedisTemplate*/
        log.info("开始创建redis模板对象...");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(connectionFactory);
        //设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // 使用GenericJackson2JsonRedisSerializer来序列化value
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());


        return redisTemplate;
    }
}
