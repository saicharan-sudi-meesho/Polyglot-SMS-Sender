package com.polyglot.sms.sender.service;

import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisService {
    
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isBlocked(String userId){
        try{
            Boolean isBlocked = redisTemplate.hasKey(userId);
            return isBlocked != null && isBlocked;
        }catch(Exception e){
            log.error("Redis is down or unreachable while for user: {}", userId, e);
            throw new RuntimeException("Redis is unreachable", e);
        }
    }

}
