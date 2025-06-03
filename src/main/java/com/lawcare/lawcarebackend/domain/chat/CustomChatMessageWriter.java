package com.lawcare.lawcarebackend.domain.chat;

import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import com.lawcare.lawcarebackend.domain.chat.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class CustomChatMessageWriter implements ItemWriter<ChatMessage> {

    private static final Logger logger = LoggerFactory.getLogger(CustomChatMessageWriter.class);
    private final ChatMessageRepository repository;

    public CustomChatMessageWriter(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void write(Chunk<? extends ChatMessage> items) throws Exception {
        try {
            List<ChatMessage> savedMessages = (List<ChatMessage>) repository.saveAll(items);
            logger.info("MySQL에 {} 개 메시지 일괄 저장 완료", savedMessages.size());

            items.forEach(item ->
                logger.info("아카이브 완료: roomId={}, content={}",
                    item.getRoomId(), item.getContent())
            );
        } catch (Exception e) {
            logger.error("메시지 저장 중 오류 발생", e);
            throw e;
        }
    }
}