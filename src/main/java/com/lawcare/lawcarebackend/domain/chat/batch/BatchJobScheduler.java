package com.lawcare.lawcarebackend.domain.chat.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job archiveChatMessagesJob;

    public BatchJobScheduler(JobLauncher jobLauncher, Job archiveChatMessagesJob) {
        this.jobLauncher = jobLauncher;
        this.archiveChatMessagesJob = archiveChatMessagesJob;
    }

    @Scheduled(cron = "0 */5 * * * ?") // 5분
    public void scheduleJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
        jobLauncher.run(archiveChatMessagesJob, jobParameters);
    }
}