package com.lawcare.lawcarebackend.domain.chat.batch;

import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisChatMessageReader implements ItemReader<ChatMessageResponseDTO> {

    private static final String CHAT_MESSAGES_KEY = "chat:messages";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisChatMessageReader(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public ChatMessageResponseDTO read() {
        return (ChatMessageResponseDTO) redisTemplate.opsForList().rightPop(CHAT_MESSAGES_KEY);
    }
}
