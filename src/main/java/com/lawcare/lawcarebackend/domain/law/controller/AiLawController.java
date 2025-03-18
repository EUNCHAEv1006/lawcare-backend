package com.lawcare.lawcarebackend.domain.law.controller;

import com.lawcare.lawcarebackend.common.dto.SuccessResponse;
import com.lawcare.lawcarebackend.domain.law.dto.request.AiLawExplainRequestDTO;
import com.lawcare.lawcarebackend.domain.law.dto.response.AiLawExplainResponseDTO;
import com.lawcare.lawcarebackend.domain.law.service.AiLawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/law")
public class AiLawController {

    private static final Logger logger = LoggerFactory.getLogger(AiLawController.class);
    private final AiLawService aiLawService;

    public AiLawController(AiLawService aiLawService) {
        this.aiLawService = aiLawService;
    }

    @Operation(summary = "법률 용어 설명 생성",
        description = "BERT로 키워드 분석 후 GPT로 최종 설명을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "법률 용어 설명 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/explain")
    public ResponseEntity<SuccessResponse<AiLawExplainResponseDTO>> explainLawTerm(
        @Valid @RequestBody AiLawExplainRequestDTO requestDTO,
        HttpServletRequest request
    ) {
        logger.info("AI 법률 설명 요청: {}", requestDTO.getUserQuestion());

        AiLawExplainResponseDTO aiResult = aiLawService.getLawExplanation(requestDTO);

        SuccessResponse<AiLawExplainResponseDTO> response = SuccessResponse.of(
            HttpStatus.OK.value(),
            "AI 법률 용어 설명 생성 완료",
            aiResult,
            request.getRequestURI()
        );
        return ResponseEntity.ok(response);
    }
}
