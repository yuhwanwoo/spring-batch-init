package com.example.springbatch.limitallow;

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
public class LimitAllowConfiguration {

    @Bean
    public Job limitAllowJob(JobRepository jobRepository, 
                             Step limitAllowStep1, 
                             Step limitAllowStep2) {
        return new JobBuilder("limitAllowJob", jobRepository)
                .start(limitAllowStep1)
                .next(limitAllowStep2)
                .build();
    }

    @Bean
    public Step limitAllowStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("limitAllowStep1", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("stepContribution = " + stepContribution + ", chunkContext = " + chunkContext);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                // 성공한 Step이라도 Job 재시작 시 항상 다시 실행하도록 설정
                .allowStartIfComplete(true) 
                .build();
    }

    @Bean
    public Step limitAllowStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("limitAllowStep2", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("stepContribution = " + stepContribution + ", chunkContext = " + chunkContext);
                    
                    // 테스트를 위한 강제 예외 발생
                    throw new RuntimeException("Intentional Failure");
                    // return RepeatStatus.FINISHED;
                }, transactionManager)
                // 이 Step의 최대 실행 횟수를 3회로 제한 (3회 실패 후에는 실행 불가)
                .startLimit(3) 
                .build();
    }
}