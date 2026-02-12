package com.example.springbatch.simpleflow1;

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
public class SimpleFlowConfiguration {

    @Bean
    public Job simpleFlowBatchJob(JobRepository jobRepository, Flow simpleFlowFlow, Step simpleFlowStep3) {
        return new JobBuilder("simpleFlowBatchJob", jobRepository)
                .start(simpleFlowFlow) // Flow (Step1 -> Step2) 실행
                .next(simpleFlowStep3) // Flow 종료 후 Step3 실행
                .end()                 // Flow를 포함한 Job 구성 종료
                .build();
    }

    @Bean
    public Flow simpleFlowFlow(Step simpleFlowStep1, Step simpleFlowStep2) {
        return new FlowBuilder<Flow>("simpleFlowFlow")
                .start(simpleFlowStep1)
                .next(simpleFlowStep2)
                .end();
    }

    @Bean
    public Step simpleFlowStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step simpleFlowStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step simpleFlowStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}