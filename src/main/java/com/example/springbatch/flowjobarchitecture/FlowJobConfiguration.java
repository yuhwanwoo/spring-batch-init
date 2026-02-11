package com.example.springbatch.flowjobarchitecture;

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
public class FlowJobConfiguration {

    @Bean
    public Job flowJobBatchJob(JobRepository jobRepository, Flow flowJobFlow, Step flowJobStep3) {
        return new JobBuilder("flowJobBatchJob", jobRepository)
                .start(flowJobFlow) // Flow (Step1 -> Step2) 실행
                .next(flowJobStep3) // Flow 종료 후 Step3 실행
                .end()              // Flow를 포함한 Job 빌더 종료
                .build();
    }

    @Bean
    public Flow flowJobFlow(Step flowJobStep1, Step flowJobStep2) {
        return new FlowBuilder<Flow>("flowJobFlow")
                .start(flowJobStep1)
                .next(flowJobStep2)
                .end();
    }

    @Bean
    public Step flowJobStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step flowJobStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowJobStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step flowJobStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowJobStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}