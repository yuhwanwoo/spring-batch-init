package com.example.springbatch.executioncontext;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ExecutionContextConfiguration {

    // Tasklet들은 컴포넌트(Bean)로 등록되어 있다고 가정하고 생성자 주입을 받습니다.
    private final ExecutionContextTasklet1 executionContextTasklet1;
    private final ExecutionContextTasklet2 executionContextTasklet2;
    private final ExecutionContextTasklet3 executionContextTasklet3;
    private final ExecutionContextTasklet4 executionContextTasklet4;

    @Bean
    public Job executionContextJob(JobRepository jobRepository,
                                   Step executionContextStep1,
                                   Step executionContextStep2,
                                   Step executionContextStep3,
                                   Step executionContextStep4) {
        return new JobBuilder("executionContextJob", jobRepository)
                .start(executionContextStep1)
                .next(executionContextStep2)
                .next(executionContextStep3)
                .next(executionContextStep4)
                .build();
    }

    @Bean
    public Step executionContextStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("executionContextStep1", jobRepository)
                .tasklet(executionContextTasklet1, transactionManager)
                .build();
    }

    @Bean
    public Step executionContextStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("executionContextStep2", jobRepository)
                .tasklet(executionContextTasklet2, transactionManager)
                .build();
    }

    @Bean
    public Step executionContextStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("executionContextStep3", jobRepository)
                .tasklet(executionContextTasklet3, transactionManager)
                .build();
    }

    @Bean
    public Step executionContextStep4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("executionContextStep4", jobRepository)
                .tasklet(executionContextTasklet4, transactionManager)
                .build();
    }
}