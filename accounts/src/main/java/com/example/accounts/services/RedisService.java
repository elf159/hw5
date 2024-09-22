package com.example.accounts.services;

import com.example.accounts.dto.TransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private final RedisTemplate<String, TransactionDTO> redisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, TransactionDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveCache(String key, TransactionDTO response) {
        ValueOperations<String, TransactionDTO> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, response, 30, TimeUnit.SECONDS);
    }

    public TransactionDTO getCache(String key) {
        ValueOperations<String, TransactionDTO> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public boolean contain(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
