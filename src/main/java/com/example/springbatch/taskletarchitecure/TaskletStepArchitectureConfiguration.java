package com.example.springbatch.taskletarchitecure;

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
public class TaskletStepArchitectureConfiguration {

    @Bean
    public Job taskletStepArchitectureJob(JobRepository jobRepository, 
                                          Step taskletStepArchitectureStep1, 
                                          Step taskletStepArchitectureStep2) {
        return new JobBuilder("taskletStepArchitectureJob", jobRepository)
                .start(taskletStepArchitectureStep1)
                .next(taskletStepArchitectureStep2)
                .build();
    }

    @Bean
    public Step taskletStepArchitectureStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStepArchitectureStep1", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("stepContribution = " + stepContribution + ", chunkContext = " + chunkContext);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                // 성공했어도 재시작 시 다시 실행됨
                .allowStartIfComplete(true) 
                .build();
    }

    @Bean
    public Step taskletStepArchitectureStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStepArchitectureStep2", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("stepContribution = " + stepContribution + ", chunkContext = " + chunkContext);
                    
                    // 테스트를 위한 강제 예외 발생
                    throw new RuntimeException("Intentional Failure");
                    // return RepeatStatus.FINISHED;
                }, transactionManager)
                // 실행 횟수 3회 제한 (3번 실패하면 더 이상 실행 불가)
                .startLimit(3) 
                .build();
    }
}