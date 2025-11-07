package com.lawcare.lawcarebackend.domain.chat.scheduler;

import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import com.lawcare.lawcarebackend.domain.chat.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Redis 캐시 메시지 정리 스케줄러
 * - 목적: Redis 메모리 효율 관리 및 DB 정합성 보장
 * - 주기: 5분마다 실행
 */
@Component
@Slf4j
public class ChatCleanupScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageRepository chatMessageRepository;

    private static final int REDIS_MESSAGE_LIMIT = 100; // Redis에 유지할 최대 메시지 수

    public ChatCleanupScheduler(RedisTemplate<String, Object> redisTemplate,
                                ChatMessageRepository chatMessageRepository) {
        this.redisTemplate = redisTemplate;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Redis 메시지 정리 및 DB 동기화
     * - 각 채팅방의 최근 100개 메시지만 Redis에 유지
     * - 100개 초과분은 Redis에서 제거 (DB에는 이미 저장되어 있음)
     * - 혹시 모를 누락 메시지 복구
     */
    @Scheduled(cron = "0 */5 * * * ?") // 5분
    @Transactional
    public void cleanupOldMessages() {
        log.info("=== Redis 메시지 정리 시작 ===");

        Set<String> roomKeys = redisTemplate.keys("chat:room:*");
        if (roomKeys == null || roomKeys.isEmpty()) {
            log.info("정리할 채팅방이 없습니다.");
            return;
        }

        int totalCleaned = 0;
        int totalRecovered = 0;
        int totalRooms = roomKeys.size();

        for (String roomKey : roomKeys) {
            try {
                // 1. 현재 Redis에 저장된 메시지 개수 확인
                Long messageCount = redisTemplate.opsForList().size(roomKey);
                if (messageCount == null || messageCount <= REDIS_MESSAGE_LIMIT) {
                    log.debug("채팅방 {} - 메시지 {}개 (정리 불필요)", roomKey, messageCount);
                    continue;
                }

                // 2. 100개 초과분 처리
                long excessCount = messageCount - REDIS_MESSAGE_LIMIT;
                log.info("채팅방 {} - {}개 메시지 정리 시작", roomKey, excessCount);

                for (int i = 0; i < excessCount; i++) {
                    // 가장 오래된 메시지 가져오기 (List의 오른쪽 끝)
                    Object message = redisTemplate.opsForList().rightPop(roomKey);

                    if (message instanceof ChatMessageResponseDTO dto) {
                        // 3. DB에 이미 저장되어 있는지 확인
                        boolean existsInDB = chatMessageRepository
                            .existsByContentAndRoomIdAndSender(
                                dto.getContent(),
                                dto.getRoomId(),
                                dto.getSender()
                            );

                        // 4. DB에 없는 경우 복구 (혹시 모를 누락 방지)
                        if (!existsInDB) {
                            ChatMessage entity = new ChatMessage(
                                dto.getType(),
                                dto.getSender(),
                                dto.getContent(),
                                dto.getRoomId()
                            );
                            chatMessageRepository.save(entity);
                            totalRecovered++;
                            log.warn("누락 메시지 복구 - roomId: {}, content: {}",
                                dto.getRoomId(), dto.getContent().substring(0, Math.min(20, dto.getContent().length())));
                        }

                        totalCleaned++;
                    }
                }

            } catch (Exception e) {
                log.error("채팅방 {} 정리 중 오류 발생: {}", roomKey, e.getMessage(), e);
            }
        }

        log.info("=== Redis 정리 완료 - 처리 방: {}, 제거: {}건, 복구: {}건 ===",
            totalRooms, totalCleaned, totalRecovered);
    }

    /**
     * 상담 종료 시 해당 채팅방 메시지 전체 아카이빙
     * - 상담이 완료되면 즉시 호출되어야 함
     * - Redis의 모든 메시지를 DB에 저장하고 Redis 정리
     */
    @Transactional
    public void archiveConsultationRoom(String roomId) {
        log.info("상담 종료 - roomId: {} 아카이빙 시작", roomId);

        String key = "chat:room:" + roomId;

        try {
            // 1. Redis의 모든 메시지 가져오기
            Long messageCount = redisTemplate.opsForList().size(key);
            if (messageCount == null || messageCount == 0) {
                log.warn("아카이빙할 메시지가 없습니다 - roomId: {}", roomId);
                return;
            }

            int saved = 0;
            int skipped = 0;

            // 2. 모든 메시지 DB 저장 확인
            for (int i = 0; i < messageCount; i++) {
                Object message = redisTemplate.opsForList().index(key, i);

                if (message instanceof ChatMessageResponseDTO dto) {
                    boolean existsInDB = chatMessageRepository
                        .existsByContentAndRoomIdAndSender(
                            dto.getContent(),
                            dto.getRoomId(),
                            dto.getSender()
                        );

                    if (!existsInDB) {
                        ChatMessage entity = new ChatMessage(
                            dto.getType(),
                            dto.getSender(),
                            dto.getContent(),
                            dto.getRoomId()
                        );
                        chatMessageRepository.save(entity);
                        saved++;
                    } else {
                        skipped++;
                    }
                }
            }

            // 3. Redis에서 해당 채팅방 전체 삭제
            redisTemplate.delete(key);

            log.info("상담 종료 아카이빙 완료 - roomId: {}, 저장: {}건, 중복: {}건",
                roomId, saved, skipped);

        } catch (Exception e) {
            log.error("상담 아카이빙 중 오류 발생 - roomId: {}, error: {}",
                roomId, e.getMessage(), e);
            throw new IllegalStateException("상담 아카이빙에 실패했습니다.", e);
        }
    }
}