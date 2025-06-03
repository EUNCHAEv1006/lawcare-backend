package com.lawcare.lawcarebackend.domain.chat.batch;

import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class ChatBatchJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(ChatBatchJobConfig.class);
    private static final String JOB_NAME = "cleanupOldChatMessagesJob";

    @Bean
    public Job cleanupOldChatMessagesJob(JobRepository jobRepository,
                                         Step cleanupChatMessagesStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(cleanupChatMessagesStep)
            .build();
    }

    @Bean
    public Step cleanupChatMessagesStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        ItemReader<ChatMessageResponseDTO> reader,
                                        ItemProcessor<ChatMessageResponseDTO, ChatMessageResponseDTO> processor,
                                        ItemWriter<ChatMessageResponseDTO> writer) {
        return new StepBuilder("cleanupChatMessagesStep", jobRepository)
            .<ChatMessageResponseDTO, ChatMessageResponseDTO>chunk(10, transactionManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }

    @Bean
    @StepScope
    public ItemReader<ChatMessageResponseDTO> redisChatMessageReader(RedisTemplate<String, Object> redisTemplate) {
        return new RedisChatMessageReader(redisTemplate);
    }

    @Bean
    public ItemProcessor<ChatMessageResponseDTO, ChatMessageResponseDTO> chatMessageProcessor() {
        return item -> {
            logger.info("Redis 메시지 정리 중: {}", item.getContent());
            return item; // 이미 DB에 저장되어 있으므로 Redis에서만 제거
        };
    }

    @Bean
    public ItemWriter<ChatMessageResponseDTO> chatMessageWriter() {
        return items -> {
            items.forEach(item ->
                logger.info("Redis에서 정리된 메시지: {} (DB에는 보존됨)", item.getContent())
            );
            // Redis에서는 제거되었지만 DB에는 영구 보존됨
        };
    }
}