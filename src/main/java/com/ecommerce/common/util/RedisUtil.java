package com.ecommerce.common.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisUtil {

  // Redis 에서 모든 데이터 String 으로 저장하고 조회하기 위해 StringRedisTemplate 사용
  private final StringRedisTemplate redisTemplate;

  // 조회
  public String getData(String key) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    return valueOperations.get(key);
  }

  // 저장
  public void setData(String key, String value, Long durationInSeconds) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    if (durationInSeconds != null) {
      Duration expireDuration = Duration.ofSeconds(durationInSeconds);
      valueOperations.set(key, value, expireDuration);
    } else {
      valueOperations.set(key, value);
    }
  }

  // 삭제
  public void deleteData(String key) {
    redisTemplate.delete(key);
  }
}
