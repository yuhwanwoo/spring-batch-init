package com.example.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class JobInstanceConfiguration {

    @Bean
    public Job batchJob(JobRepository jobRepository, Step batchStep1, Step batchStep2) {
        return new JobBuilder("batchJob", jobRepository)
                .start(batchStep1)
                .next(batchStep2)
                .build();
    }

    @Bean
    public Step batchStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // JobInstance 정보 가져오기 (로직 동일)
                    JobInstance jobInstance = contribution.getStepExecution().getJobExecution().getJobInstance();

                    System.out.println("jobInstance.getId() : " + jobInstance.getId());
                    System.out.println("jobInstance.getInstanceId() : " + jobInstance.getInstanceId());
                    System.out.println("jobInstance.getJobName() : " + jobInstance.getJobName());
                    System.out.println("jobInstance.getJobVersion : " + jobInstance.getVersion());

                    return RepeatStatus.FINISHED;
                }, transactionManager) // transactionManager 필수
                .build();
    }

    @Bean
    public Step batchStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager) // transactionManager 필수
                .build();
    }
}
