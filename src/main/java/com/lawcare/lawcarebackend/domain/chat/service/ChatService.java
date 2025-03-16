package com.lawcare.lawcarebackend.domain.chat.service;

import com.lawcare.lawcarebackend.domain.chat.dto.request.ChatMessageRequestDTO;
import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final String CHAT_MESSAGES_KEY_PREFIX = "chat:room:";

    private final RedisTemplate<String, Object> redisTemplate;

    public ChatService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 채팅 메시지를 처리합니다.
     * - 채팅 메시지는 roomId를 포함하여 저장됩니다.
     * - 저장된 메시지는 추후 Spring Batch 작업이 해당 roomId별로 처리할 수 있습니다.
     *
     * @param requestDTO 채팅 메시지 요청 DTO (메시지 타입, 내용, 채팅 방 ID 포함)
     * @param auth       인증 정보 (전송자의 정보)
     * @return 처리된 채팅 메시지 응답 DTO
     */
    public ChatMessageResponseDTO sendChatMessage(ChatMessageRequestDTO requestDTO, Authentication auth) {
        try {
            String sender = (auth != null) ? auth.getName() : "알수없음";
            // 요청 DTO의 roomId 값 사용. (일대일 채팅의 경우, 두 사용자의 고유한 대화방 ID가 들어감)
            String roomId = requestDTO.getRoomId();
            ChatMessageResponseDTO response = new ChatMessageResponseDTO(
                requestDTO.getType(),
                sender,
                requestDTO.getContent(),
                roomId
            );

            String key = CHAT_MESSAGES_KEY_PREFIX + roomId;
            redisTemplate.opsForList().leftPush(key, response);
            logger.info("채팅 메시지 저장 완료 - roomId: {}, sender: {}, type: {}, content: {}",
                roomId, sender, requestDTO.getType(), requestDTO.getContent());

            return response;
        } catch (Exception e) {
            logger.error("채팅 메시지 처리 중 예외 발생", e);
            throw new IllegalArgumentException("채팅 메시지 처리에 실패했습니다.", e);
        }
    }
}
