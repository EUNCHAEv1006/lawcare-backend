package com.lawcare.lawcarebackend.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "채팅 메시지 요청 DTO")
public class ChatMessageRequestDTO {

    @Schema(description = "메시지 타입 (예: CHAT, JOIN, LEAVE)", example = "CHAT")
    @NotBlank(message = "메시지 타입은 필수입니다.")
    private String type;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Schema(example = "안녕하세요.", description = "메시지 내용")
    private String content;

    @Schema(description = "채팅 방 ID", example = "123")
    private String roomId;

    public ChatMessageRequestDTO() {
    }
}
