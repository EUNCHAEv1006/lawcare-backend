package com.lawcare.lawcarebackend.domain.chat.service;

import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import com.lawcare.lawcarebackend.domain.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatHistoryService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<ChatMessage> getChatHistory(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }
}