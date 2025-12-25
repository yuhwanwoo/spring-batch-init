package com.example.springbatch.jobexecution;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
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
public class JobExecutionConfiguration {

    @Bean
    public Job executionJob(JobRepository jobRepository, Step executionStep1, Step executionStep2) {
        return new JobBuilder("executionJob", jobRepository)
                .start(executionStep1)
                .next(executionStep2)
                .build();
    }

    @Bean
    public Step executionStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("executionStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
                    System.out.println("jobExecution = " + jobExecution);
                    System.out.println("executionJob1 has executed");

                    return RepeatStatus.FINISHED;
                }, transactionManager) // TransactionManager 명시
                .build();
    }

    @Bean
    public Step executionStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("executionStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    // throw new RuntimeException("JobExecution has failed");
                    System.out.println("executionJob2 has executed");

                    return RepeatStatus.FINISHED;
                }, transactionManager) // TransactionManager 명시
                .build();
    }
}
