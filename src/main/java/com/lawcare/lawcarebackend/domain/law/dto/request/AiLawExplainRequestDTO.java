package com.lawcare.lawcarebackend.domain.law.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "법률 용어 설명 요청 DTO")
public class AiLawExplainRequestDTO {

    @NotBlank(message = "질문 내용은 필수입니다.")
    @Schema(example = "최저임금", description = "사용자가 어려워하는 법률 용어 ")
    private String userQuestion;
}
