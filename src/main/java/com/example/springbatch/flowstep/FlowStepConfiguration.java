package com.example.springbatch.flowstep;

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
public class FlowStepConfiguration {

    @Bean
    public Job flowStepBatchJob(JobRepository jobRepository, 
                                Step flowStepFlowStep, 
                                Step flowStepStep2) {
        return new JobBuilder("flowStepBatchJob", jobRepository)
                .start(flowStepFlowStep) // FlowStep 실행
                .next(flowStepStep2)
                .build();
    }

    // Flow를 품은 Step (FlowStep)
    @Bean
    public Step flowStepFlowStep(JobRepository jobRepository, Flow flowStepFlow) {
        return new StepBuilder("flowStepFlowStep", jobRepository)
                .flow(flowStepFlow) // Flow 주입
                .build();
    }

    // 실제 Flow 정의
    @Bean
    public Flow flowStepFlow(Step flowStepStep1) {
        return new FlowBuilder<Flow>("flowStepFlow")
                .start(flowStepStep1)
                .end();
    }

    @Bean
    public Step flowStepStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowStepStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 was executed");
                    
                    // 기존 로직 유지: 예외 발생
                    throw new RuntimeException("step1 was failed");
                    // return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step flowStepStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowStepStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 was executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}