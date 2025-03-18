package com.lawcare.lawcarebackend.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "번역 요청 DTO")
public class ChatMessageTranslateRequestDTO {

    @Schema(example = "안녕하세요", description = "원본 메시지")
    private String originalMessage;

    @Schema(example = "en", description = "번역 대상 언어 코드 (예: en, ko, ja)")
    private String targetLanguage;
}