package com.lawcare.lawcarebackend.domain.chat.repository;

import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomIdOrderByCreatedAtDesc(String roomId);

    List<ChatMessage> findByCreatedAtAfterOrderByCreatedAtAsc(LocalDateTime cutoffTime);

    boolean existsByContentAndRoomIdAndSender(String content, String roomId, String sender);
}