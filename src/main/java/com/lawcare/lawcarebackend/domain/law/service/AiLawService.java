package com.lawcare.lawcarebackend.domain.law.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lawcare.lawcarebackend.domain.law.dto.request.AiLawExplainRequestDTO;
import com.lawcare.lawcarebackend.domain.law.dto.response.AiLawExplainResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiLawService {

    private static final Logger logger = LoggerFactory.getLogger(AiLawService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.huggingface.endpoint}")
    private String hfEndpoint;

    @Value("${ai.huggingface.token}")
    private String hfToken;

    @Value("${ai.openai.token}")
    private String openaiToken;

    /**
     * 사용자의 질문을 받아, BERT 통해 키워드/문맥을 추출하고,
     * GPT(OpenAI API)로부터 최종 설명을 생성받아 반환
     */
    public AiLawExplainResponseDTO getLawExplanation(AiLawExplainRequestDTO requestDTO) {
        String userQuestion = requestDTO.getUserQuestion();

        try {
            // 1) BERT 호출 → 키워드/문맥 분석
            String extractedKeywords = callBertInference(userQuestion);

            // 2) GPT 호출 → 최종 설명 문장 생성
            String explanation = callGptApi(userQuestion, extractedKeywords);

            return new AiLawExplainResponseDTO(explanation, extractedKeywords);

        } catch (Exception e) {
            logger.error("AI 법률 용어 설명 중 예외 발생: {}", e.getMessage(), e);
            throw new IllegalStateException("법률 용어 설명 생성에 실패했습니다.", e);
        }
    }

    /**
     * 1) Hugging Face Inference API를 이용해 BERT 모델 호출
     * - 사용자가 물어본 문장에서 핵심 키워드를 뽑거나 요약(개념) 추출
     */
    private String callBertInference(String userQuestion) throws Exception {
        logger.info("BERT 키워드 분석 요청: {}", userQuestion);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(hfToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = objectMapper.writeValueAsString(
            new HfInput(userQuestion)
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate
            .postForEntity(hfEndpoint, requestEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("BERT API 호출 실패");
        }

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        String keywords = "키워드를 찾지 못함";
        if (jsonNode.isArray() && !jsonNode.isEmpty()) {
            keywords = jsonNode.get(0).asText();
        }

        logger.info("BERT 분석 결과: {}", keywords);
        return keywords;
    }

    /**
     * 2) OpenAI GPT API 호출 → 최종 설명 문장 생성
     */
    private String callGptApi(String userQuestion, String extractedKeywords) throws Exception {
        logger.info("GPT 설명 생성 요청. userQuestion={}, keywords={}",
            userQuestion, extractedKeywords);

        String prompt = String.format(
            """
                당신은 한국 법률 전문가입니다. \
                다음 키워드를 중심으로 간단하고 정확하게 설명해 주세요.

                키워드: %s
                사용자 질문: %s

                어린이도 이해할 수 있을만큼 친절하고 쉽게 설명을 작성해 주세요.""",
            extractedKeywords, userQuestion
        );

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openaiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String gptRequestBody = objectMapper.writeValueAsString(
            new OpenAiChatRequest("user", prompt)
        );

        HttpEntity<String> entity = new HttpEntity<>(gptRequestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "https://api.openai.com/v1/chat/completions",
            entity,
            String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("GPT API 호출 실패");
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty()) {
            String content = choices.get(0).path("message").path("content").asText();
            logger.info("GPT 생성 결과: {}", content);
            return content.trim();
        } else {
            throw new RuntimeException("GPT 응답 파싱 실패");
        }
    }

    static class HfInput {
        public String text;

        public HfInput(String text) {
            this.text = text;
        }
    }

    static class OpenAiChatRequest {
        public String model = "gpt-3.5-turbo";
        public OpenAiMessage[] messages;

        public OpenAiChatRequest(String role, String content) {
            this.messages = new OpenAiMessage[]{
                new OpenAiMessage(role, content)
            };
        }
    }

    static class OpenAiMessage {
        public String role;
        public String content;

        public OpenAiMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}