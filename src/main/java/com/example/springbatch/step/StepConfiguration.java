package com.example.springbatch.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class StepConfiguration {

    @Bean
    public Job stepJob(JobRepository jobRepository, Step stepStep1, Step stepStep2) {
        return new JobBuilder("stepJob", jobRepository)
                .start(stepStep1)
                .next(stepStep2)
                .build();
    }

    @Bean
    public Step stepStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepStep1", jobRepository)
                .tasklet(new CustomTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step stepStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("stepStep2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}