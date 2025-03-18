package com.lawcare.lawcarebackend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "번역 결과 DTO")
public class TranslationResponseDTO {

    @Schema(example = "Hello", description = "번역된 텍스트")
    private String translatedText;

    @Schema(example = "ko", description = "감지된 원본 언어 코드")
    private String detectedSourceLanguage;
}