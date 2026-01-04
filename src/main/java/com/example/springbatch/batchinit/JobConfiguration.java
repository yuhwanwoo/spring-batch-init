package com.example.springbatch.batchinit;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class JobConfiguration {

    @Bean
    public Job batchJob1(JobRepository jobRepository, Step jobConfigStep1, Step jobConfigStep2) {
        return new JobBuilder("batchJob1", jobRepository)
                .incrementer(new RunIdIncrementer()) // RunIdIncrementer 유지
                .start(jobConfigStep1)
                .next(jobConfigStep2)
                .build();
    }

    @Bean
    public Step jobConfigStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobConfigStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager) // TransactionManager 명시
                .build();
    }

    @Bean
    public Step jobConfigStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobConfigStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager) // TransactionManager 명시
                .build();
    }
}