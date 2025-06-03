package com.lawcare.lawcarebackend.domain.chat.service;

import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import com.lawcare.lawcarebackend.domain.chat.repository.ChatMessageRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedisRecoveryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private volatile boolean isRedisAvailable = true;

    public RedisRecoveryService(RedisTemplate<String, Object> redisTemplate,
                                ChatMessageRepository chatMessageRepository) {
        this.redisTemplate = redisTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 애플리케이션 시작 시 Redis 데이터 복원
     */
    @PostConstruct
    public void initializeRedisData() {
        log.info("애플리케이션 시작 - Redis 데이터 복원 시작");
        restoreRecentMessages();
    }

    /**
     * 수동으로 Redis 복구 호출 (관리자용 API)
     */
    public void manualRestore() {
        log.info("수동 Redis 데이터 복원 요청");
        restoreRecentMessages();
    }

    /**
     * 최근 1시간 메시지를 DB에서 Redis로 복원
     */
    public void restoreRecentMessages() {
        try {
            // Redis 연결 상태 확인
            redisTemplate.opsForValue().set("restore:test", "ok", Duration.ofSeconds(10));

            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            List<ChatMessage> recentMessages = chatMessageRepository
                .findByCreatedAtAfterOrderByCreatedAtAsc(cutoffTime);

            Map<String, List<ChatMessage>> messagesByRoom = recentMessages.stream()
                                                                          .collect(Collectors.groupingBy(ChatMessage::getRoomId));

            int restoredCount = 0;
            for (Map.Entry<String, List<ChatMessage>> entry : messagesByRoom.entrySet()) {
                String roomId = entry.getKey();
                String key = "chat:room:" + roomId;

                // 기존 Redis 데이터 초기화
                redisTemplate.delete(key);

                for (ChatMessage message : entry.getValue()) {
                    ChatMessageResponseDTO dto = new ChatMessageResponseDTO(
                        message.getType(),
                        message.getSender(),
                        message.getContent(),
                        message.getRoomId()
                    );
                    redisTemplate.opsForList().leftPush(key, dto);
                    restoredCount++;
                }
            }

            isRedisAvailable = true;
            log.info("Redis 데이터 복원 완료: {} 건", restoredCount);

        } catch (Exception e) {
            isRedisAvailable = false;
            log.error("Redis 데이터 복원 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * Redis 상태 확인
     */
    public boolean isRedisAvailable() {
        return isRedisAvailable;
    }

    /**
     * Redis 상태 업데이트
     */
    public void updateRedisStatus(boolean available) {
        this.isRedisAvailable = available;
        if (available) {
            log.info("Redis 연결 복구 감지 - 데이터 복원 시작");
            restoreRecentMessages();
        }
    }
}