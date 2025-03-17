package com.lawcare.lawcarebackend.domain.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lawcare.lawcarebackend.domain.chat.dto.request.ChatMessageRequestDTO;
import com.lawcare.lawcarebackend.domain.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            logger.info("웹소켓 연결 성공: {}", session.getId());
        } catch (Exception e) {
            logger.error("웹소켓 연결 중 예외 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("메시지 수신: {} / 연결 ID: {}", message.getPayload(), session.getId());

        // 클라이언트가 JSON 형식의 메시지를 전송해야 합니다.
        // 예: { "type": "CHAT", "content": "안녕하세요", "roomId": "room123" }
        ChatMessageRequestDTO requestDTO = objectMapper.readValue(message.getPayload(), ChatMessageRequestDTO.class);

        // 연결된 session을 roomId에 등록 (최초 메시지 수신 시 등록)
        String roomId = requestDTO.getRoomId();
        roomSessions.computeIfAbsent(roomId, key -> new CopyOnWriteArrayList<>()).add(session);

        chatService.sendChatMessage(requestDTO, null);

        // 해당 roomId에 속한 모든 연결된 session에 메시지 브로드캐스트
        TextMessage broadcastMessage = new TextMessage("[" + roomId + "] " + requestDTO.getContent());
        for (WebSocketSession wsSession : roomSessions.get(roomId)) {
            if (wsSession.isOpen()) {
                // 동기화 블록으로 각 세션에 대한 sendMessage 호출을 순차적으로 처리
                synchronized (wsSession) {
                    wsSession.sendMessage(broadcastMessage);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            logger.info("웹소켓 연결 종료: {} / 상태: {}", session.getId(), status);
            roomSessions.forEach((roomId, sessions) -> sessions.remove(session));
        } catch (Exception e) {
            logger.error("웹소켓 연결 종료 처리 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
