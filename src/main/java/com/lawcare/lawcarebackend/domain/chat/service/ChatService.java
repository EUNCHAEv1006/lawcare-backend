package com.lawcare.lawcarebackend.domain.chat.service;

import com.lawcare.lawcarebackend.common.dto.TranslationResponseDTO;
import com.lawcare.lawcarebackend.domain.chat.dto.request.ChatMessageRequestDTO;
import com.lawcare.lawcarebackend.domain.chat.dto.request.ChatMessageTranslateRequestDTO;
import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import com.lawcare.lawcarebackend.domain.chat.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String CHAT_MESSAGES_KEY_PREFIX = "chat:room:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final TranslationService translationService;
    private final ChatMessageRepository chatMessageRepository; // 추가

    public ChatService(RedisTemplate<String, Object> redisTemplate,
                       TranslationService translationService,
                       ChatMessageRepository chatMessageRepository) {
        this.redisTemplate = redisTemplate;
        this.translationService = translationService;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * 개선된 채팅 메시지 저장
     */
    public ChatMessageResponseDTO sendChatMessage(ChatMessageRequestDTO requestDTO, Authentication auth) {
        String sender = (auth != null) ? auth.getName() : "알수없음";
        String roomId = requestDTO.getRoomId();

        ChatMessageResponseDTO response = new ChatMessageResponseDTO(
            requestDTO.getType(),
            sender,
            requestDTO.getContent(),
            roomId
        );

        // 1단계: DB에 먼저 저장 (데이터 보존 우선)
        try {
            ChatMessage entity = new ChatMessage(
                requestDTO.getType(),
                sender,
                requestDTO.getContent(),
                roomId
            );
            chatMessageRepository.save(entity);
            logger.info("DB 저장 완료 - roomId: {}", roomId);
        } catch (Exception dbException) {
            logger.error("DB 저장 실패: {}", dbException.getMessage());
            throw new IllegalStateException("메시지 저장에 실패했습니다.", dbException);
        }

        // 2단계: Redis에 저장 (성능 최적화용, 실패해도 서비스 지속)
        try {
            String key = CHAT_MESSAGES_KEY_PREFIX + roomId;
            redisTemplate.opsForList().leftPush(key, response);
            logger.info("Redis 저장 완료 - roomId: {}", roomId);
        } catch (Exception redisException) {
            logger.warn("Redis 저장 실패하지만 서비스 계속: {}", redisException.getMessage());
            // Redis 실패해도 DB에는 저장되었으므로 서비스 계속
        }

        return response;
    }

    /**
     * 메시지 조회 (Redis 우선, 실패 시 DB 조회)
     */
    public List<ChatMessageResponseDTO> getRecentMessages(String roomId, int limit) {
        try {
            // 1차: Redis에서 조회 시도
            String key = CHAT_MESSAGES_KEY_PREFIX + roomId;
            List<Object> redisMessages = redisTemplate.opsForList().range(key, 0, limit - 1);

            if (redisMessages != null && !redisMessages.isEmpty()) {
                logger.info("Redis에서 메시지 조회 성공 - count: {}", redisMessages.size());
                return redisMessages.stream()
                                    .map(obj -> (ChatMessageResponseDTO) obj)
                                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.warn("Redis 조회 실패, DB로 전환: {}", e.getMessage());
        }

        // 2차: Redis 실패 시 DB에서 조회
        try {
            List<ChatMessage> dbMessages = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
            logger.info("DB에서 메시지 조회 - count: {}", dbMessages.size());

            return dbMessages.stream()
                             .limit(limit)
                             .map(msg -> new ChatMessageResponseDTO(
                                 msg.getType(),
                                 msg.getSender(),
                                 msg.getContent(),
                                 msg.getRoomId()
                             ))
                             .collect(Collectors.toList());
        } catch (Exception dbException) {
            logger.error("DB 조회도 실패", dbException);
            return Collections.emptyList();
        }
    }

    // 기존 translateChatMessage 메서드는 그대로 유지
    public TranslationResponseDTO translateChatMessage(ChatMessageTranslateRequestDTO requestDTO) {
        try {
            logger.info("번역 요청 - original: {}, target: {}",
                requestDTO.getOriginalMessage(), requestDTO.getTargetLanguage());

            return translationService.translateMessage(
                requestDTO.getOriginalMessage(),
                requestDTO.getTargetLanguage()
            );
        } catch (Exception e) {
            logger.error("[translateChatMessage] 번역 처리 중 예외 발생", e);
            throw new IllegalArgumentException("채팅 메시지 번역에 실패했습니다.", e);
        }
    }
}