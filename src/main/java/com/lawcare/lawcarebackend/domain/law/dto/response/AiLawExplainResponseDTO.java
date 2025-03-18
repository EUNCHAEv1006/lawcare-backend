package com.lawcare.lawcarebackend.domain.law.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "법률 용어 설명 응답 DTO")
public class AiLawExplainResponseDTO {

    @Schema(example = "최저임금이란...", description = "GPT가 생성한 설명 문장")
    private String explanation;

    @Schema(example = "최저임금, 근로기준법", description = "BERT가 분석한 주요 키워드")
    private String keywords;
}