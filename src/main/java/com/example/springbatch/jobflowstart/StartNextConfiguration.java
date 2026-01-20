package com.example.springbatch.jobflowstart;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
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
    public Job startNextBatchJob(JobRepository jobRepository,
                                 Flow startNextFlowA,
                                 Step startNextStep3,
                                 Flow startNextFlowB,
                                 Step startNextStep6) {
        return new JobBuilder("startNextBatchJob", jobRepository)
                .start(startNextFlowA) // Flow A 실행
                .next(startNextStep3)  // Step 3 실행
                .next(startNextFlowB)  // Flow B 실행
                .next(startNextStep6)  // Step 6 실행
                .end()                 // Flow와 Step을 연결하여 Job을 구성할 때 end() 호출 필요
                .build();
    }

    // --- Flows ---

    @Bean
    public Flow startNextFlowA(Step startNextStep1, Step startNextStep2) {
        return new FlowBuilder<Flow>("startNextFlowA")
                .start(startNextStep1)
                .next(startNextStep2)
                .end();
    }

    @Bean
    public Flow startNextFlowB(Step startNextStep4, Step startNextStep5) {
        return new FlowBuilder<Flow>("startNextFlowB")
                .start(startNextStep4)
                .next(startNextStep5)
                .end();
    }

    // --- Steps ---

    @Bean
    public Step startNextStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startNextStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startNextStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startNextStep4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step4 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startNextStep5(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep5", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step5 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step startNextStep6(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("startNextStep6", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step6 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}