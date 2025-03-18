package com.lawcare.lawcarebackend.domain.chat.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;
import com.lawcare.lawcarebackend.common.dto.TranslationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private final Translate translate;

    public TranslationService(Translate translate) {
        this.translate = translate;
    }

    public TranslationResponseDTO translateMessage(String originalMessage, String targetLanguage) {
        try {
            logger.info("번역 요청 - original: {}, targetLang: {}", originalMessage, targetLanguage);

            Translation translation = translate.translate(
                originalMessage,
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.model("nmt")
            );

            TranslationResponseDTO dto = new TranslationResponseDTO(
                translation.getTranslatedText(),
                translation.getSourceLanguage()
            );
            logger.info("번역 성공 - result: {}", dto.getTranslatedText());
            return dto;

        } catch (Exception e) {
            logger.error("번역 요청 중 예외 발생", e);
            throw new IllegalStateException("번역 처리에 실패했습니다.", e);
        }
    }
}