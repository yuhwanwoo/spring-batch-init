package com.example.springbatch.transition;

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
public class TransitionConfiguration {

    @Bean
    public Job transitionJob(JobRepository jobRepository, Flow transitionFlow, Step transitionStep3) {
        return new JobBuilder("transitionJob", jobRepository)
                .start(transitionFlow) // Flow 실행 (Step1 -> Step2)
                .next(transitionStep3) // Flow 종료 후 Step3 실행
                .end()                 // Flow를 포함한 Job 구성 종료
                .build();
    }

    @Bean
    public Flow transitionFlow(Step transitionStep1, Step transitionStep2) {
        return new FlowBuilder<Flow>("transitionFlow")
                .start(transitionStep1)
                .next(transitionStep2)
                .end();
    }

    @Bean
    public Step transitionStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step transitionStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step transitionStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}