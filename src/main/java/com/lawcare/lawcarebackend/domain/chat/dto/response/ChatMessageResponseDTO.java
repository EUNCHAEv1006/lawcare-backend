package com.lawcare.lawcarebackend.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponseDTO {

    @Schema(example = "CHAT", description = "메시지 유형")
    private String type;

    @Schema(example = "1", description = "전송자 userId")
    private String sender;

    @Schema(example = "안녕하세요.", description = "메시지 내용")
    private String content;

    @Schema(description = "채팅 방 ID", example = "123")
    private String roomId;

    public ChatMessageResponseDTO(String type, String sender, String content, String roomId) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.roomId = roomId;
    }
}
