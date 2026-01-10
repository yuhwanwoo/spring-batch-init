package com.example.springbatch.preventrestart;

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
public class PreventRestartConfiguration {

    @Bean
    public Job preventRestartJob(JobRepository jobRepository, 
                                 Step preventRestartStep1, 
                                 Step preventRestartStep2, 
                                 Step preventRestartStep3) {
        return new JobBuilder("preventRestartJob", jobRepository)
                .start(preventRestartStep1)
                .next(preventRestartStep2)
                .next(preventRestartStep3)
                .preventRestart() // 재시작 방지 설정 유지
                .build();
    }

    @Bean
    public Step preventRestartStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("preventRestartStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step preventRestartStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("preventRestartStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step preventRestartStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("preventRestartStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 예외 발생 시 Job은 FAILED 상태가 되지만, preventRestart() 설정 때문에 재시작 불가
                    throw new RuntimeException("step3 has failed");
                    // return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}