package com.lawcare.lawcarebackend.domain.chat.batch;

import com.lawcare.lawcarebackend.domain.chat.dto.response.ChatMessageResponseDTO;
import com.lawcare.lawcarebackend.domain.chat.entity.ChatMessage;
import com.lawcare.lawcarebackend.domain.chat.repository.ChatMessageRepository;
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
import org.springframework.batch.item.data.RepositoryItemWriter;
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
                                        ItemProcessor<ChatMessageResponseDTO, ChatMessage> processor,
                                        ItemWriter<ChatMessage> writer) {
        return new StepBuilder("archiveChatMessagesStep", jobRepository)
            .<ChatMessageResponseDTO, ChatMessage>chunk(10, transactionManager)
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
    public ItemProcessor<ChatMessageResponseDTO, ChatMessage> chatMessageProcessor() {
        return item -> {
            logger.info("메시지 처리 중: {}", item.getContent());
            return new ChatMessage(
                item.getType(),
                item.getSender(),
                item.getContent(),
                item.getRoomId()
            );
        };
    }

    @Bean
    public RepositoryItemWriter<ChatMessage> chatMessageWriter(ChatMessageRepository repository) {
        RepositoryItemWriter<ChatMessage> writer = new RepositoryItemWriter<>();
        writer.setRepository(repository);
        writer.setMethodName("save");
        return writer;
    }
}
