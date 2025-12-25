package com.example.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DBJobConfiguration {

    @Bean
    public Job dbjob(JobRepository jobRepository, Step dbStep1, Step dbStep2, Step dbStep3) {
        return new JobBuilder("DbJob", jobRepository) // Factory 대신 직접 생성
                .start(dbStep1)
                .next(dbStep2)
                .next(dbStep3)
                .build();
    }

    @Bean
    public Step dbStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("dbStep1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager) // TransactionManager 필수
                .build();
    }

    @Bean
    public Step dbStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("dbStep2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step dbStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("dbStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
