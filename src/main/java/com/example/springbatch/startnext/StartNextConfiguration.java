package com.example.springbatch.startnext;

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
public class StartNextConfiguration {

    @Bean
    public Job startNextJob(JobRepository jobRepository, 
                            Step startNextStep1, 
                            Step startNextStep2, 
                            Step startNextStep3) {
        return new JobBuilder("startNextJob", jobRepository)
                .start(startNextStep1)
                .next(startNextStep2)
                .next(startNextStep3)
                .build();
    }

    @Bean
    public Step startNextStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startNextStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startNextStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}