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
    private static final String JOB_NAME = "archiveChatMessagesJob";

    @Bean
    public Job archiveChatMessagesJob(JobRepository jobRepository,
                                      Step archiveChatMessagesStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(archiveChatMessagesStep)
            .build();
    }

    @Bean
    public Step archiveChatMessagesStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        ItemReader<ChatMessageResponseDTO> reader,
                                        ItemProcessor<ChatMessageResponseDTO, ChatMessageResponseDTO> processor,
                                        ItemWriter<ChatMessageResponseDTO> writer) {
        return new StepBuilder("archiveChatMessagesStep", jobRepository)
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
            logger.info("메시지 처리 중: {}", item.getContent());
            return item;
        };
    }

    @Bean
    public ItemWriter<ChatMessageResponseDTO> chatMessageWriter() {
        return items -> items.forEach(item -> logger.info("아카이브된 메시지: {}", item.getContent()));
    }
}
