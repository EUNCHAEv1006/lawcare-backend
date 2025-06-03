package com.lawcare.lawcarebackend.domain.chat.controller;

import com.lawcare.lawcarebackend.common.dto.SuccessResponse;
import com.lawcare.lawcarebackend.common.dto.TranslationResponseDTO;
import com.lawcare.lawcarebackend.domain.chat.dto.request.ChatMessageRequestDTO;
import com.lawcare.lawcarebackend.domain.chat.dto.request.ChatMessageTranslateRequestDTO;
import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import com.lawcare.lawcarebackend.domain.chat.service.ChatHistoryService;
import com.lawcare.lawcarebackend.domain.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;
    private final ChatHistoryService chatHistoryService;

    public ChatController(ChatService chatService, ChatHistoryService chatHistoryService) {
        this.chatService = chatService;
        this.chatHistoryService = chatHistoryService;
    }

    @Operation(
        summary = "채팅 메시지 전송",
        description = "인증된 사용자가 채팅 방에 메시지를 전송합니다. 메시지 타입과 내용을 포함한 요청을 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "채팅 메시지 전송 성공",
            content = @Content(schema = @Schema(implementation = ChatMessageResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예외 발생")
    })
    @PostMapping("/send")
    public ResponseEntity<SuccessResponse<ChatMessageResponseDTO>> sendChatMessage(
        @Valid @RequestBody ChatMessageRequestDTO requestDTO,
        HttpServletRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("채팅 메시지 전송 요청: user={}, roomId={}, type={}, content={}",
            auth != null ? auth.getName() : "익명",
            requestDTO.getRoomId(),
            requestDTO.getType(),
            requestDTO.getContent());

        ChatMessageResponseDTO responseData = chatService.sendChatMessage(requestDTO, auth);
        SuccessResponse<ChatMessageResponseDTO> response = SuccessResponse.of(
            HttpStatus.OK.value(),
            "채팅 메시지 전송 성공",
            responseData,
            request.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "채팅 메시지 번역",
        description = "사용자가 원하는 언어로 메시지를 번역합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "번역 성공",
            content = @Content(schema = @Schema(implementation = TranslationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 예외 발생")
    })
    @PostMapping("/translate")
    public ResponseEntity<SuccessResponse<TranslationResponseDTO>> translateChatMessage(
        @Valid @RequestBody ChatMessageTranslateRequestDTO requestDTO,
        HttpServletRequest request
    ) {
        logger.info("번역 요청: originalMessage={}, targetLang={}",
            requestDTO.getOriginalMessage(), requestDTO.getTargetLanguage());

        TranslationResponseDTO translation = chatService.translateChatMessage(requestDTO);

        SuccessResponse<TranslationResponseDTO> response = SuccessResponse.of(
            HttpStatus.OK.value(),
            "채팅 메시지 번역 성공",
            translation,
            request.getRequestURI()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{roomId}")
    @Operation(summary = "채팅 기록 조회", description = "특정 채팅방의 메시지 기록을 조회합니다.")
    public ResponseEntity<SuccessResponse<List<ChatMessage>>> getChatHistory(
        @PathVariable String roomId,
        HttpServletRequest request
    ) {
        List<ChatMessage> history = chatHistoryService.getChatHistory(roomId);

        SuccessResponse<List<ChatMessage>> response = SuccessResponse.of(
            HttpStatus.OK.value(),
            "채팅 기록 조회 성공",
            history,
            request.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }
}