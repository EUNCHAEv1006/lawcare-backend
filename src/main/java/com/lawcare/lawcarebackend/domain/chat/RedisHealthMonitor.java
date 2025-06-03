package com.lawcare.lawcarebackend.domain.chat;

import com.lawcare.lawcarebackend.domain.chat.service.RedisRecoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class RedisHealthMonitor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisRecoveryService recoveryService;
    private boolean previousRedisStatus = true;

    public RedisHealthMonitor(RedisTemplate<String, Object> redisTemplate,
                              RedisRecoveryService recoveryService) {
        this.redisTemplate = redisTemplate;
        this.recoveryService = recoveryService;
    }

    @Scheduled(fixedDelay = 30000) // 30초마다 체크
    public void checkRedisHealth() {
        boolean currentStatus = false;

        try {
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(60));
            String result = (String) redisTemplate.opsForValue().get("health:check");
            currentStatus = "ok".equals(result);

            if (currentStatus) {
                log.debug("Redis 정상 동작 중");
            }
        } catch (Exception e) {
            log.error("Redis 연결 불가: {}", e.getMessage());
            currentStatus = false;
        }

        // 상태 변화 감지 (장애 → 복구)
        if (!previousRedisStatus && currentStatus) {
            log.info("Redis 연결 복구 감지!");
            recoveryService.updateRedisStatus(true);
        } else if (previousRedisStatus && !currentStatus) {
            log.warn("Redis 연결 장애 감지!");
            recoveryService.updateRedisStatus(false);
        }

        previousRedisStatus = currentStatus;
    }

    /**
     * 수동으로 Redis 복구 트리거 (관리자용 API)
     */
    public void triggerRedisRecovery() {
        log.info("수동 Redis 복구 트리거");
        recoveryService.manualRestore();
    }
}