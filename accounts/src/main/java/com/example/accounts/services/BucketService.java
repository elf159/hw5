package com.example.accounts.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BucketService {
    private final Map<Integer, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket get(Integer Id) {
        return buckets.computeIfAbsent(Id, this::create);
    }

    private Bucket create(Integer Id) {
        return Bucket.builder()
                .addLimit(Bandwidth
                        .builder()
                        .capacity(5)
                        .refillIntervally(5, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}
