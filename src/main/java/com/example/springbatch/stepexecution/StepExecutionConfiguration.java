package com.example.springbatch.stepexecution;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class StepExecutionConfiguration {

    @Bean
    public Job stepExecutionJob(JobRepository jobRepository,
                                Step stepExecutionStep1,
                                Step stepExecutionStep2,
                                Step stepExecutionStep3) {
        return new JobBuilder("stepExecutionJob", jobRepository)
                .start(stepExecutionStep1)
                .next(stepExecutionStep2)
                .next(stepExecutionStep3)
                .build();
    }

    @Bean
    public Step stepExecutionStep1(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   Tasklet executionContextTasklet1) {
        return new StepBuilder("stepExecutionStep1", jobRepository)
                .tasklet(executionContextTasklet1, transactionManager)
                .build();
    }

    @Bean
    public Step stepExecutionStep2(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   Tasklet executionContextTasklet2) {
        return new StepBuilder("stepExecutionStep2", jobRepository)
                .tasklet(executionContextTasklet2, transactionManager)
                .build();
    }

    @Bean
    public Step stepExecutionStep3(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   Tasklet executionContextTasklet3) {
        return new StepBuilder("stepExecutionStep3", jobRepository)
                .tasklet(executionContextTasklet3, transactionManager)
                .build();
    }

    // --- Tasklet Beans ---

    @Bean
    public Tasklet executionContextTasklet1() {
        return (contribution, chunkContext) -> {
            System.out.println(">> stepExecutionStep1 has executed");
            // StepExecution 정보 확인
            System.out.println("   StepExecution: " + contribution.getStepExecution());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet executionContextTasklet2() {
        return (contribution, chunkContext) -> {
            System.out.println(">> stepExecutionStep2 has executed");
            // StepExecution 정보 확인
            System.out.println("   StepExecution: " + contribution.getStepExecution());
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet executionContextTasklet3() {
        return (contribution, chunkContext) -> {
            System.out.println(">> stepExecutionStep3 has executed");
            // StepExecution 정보 확인
            System.out.println("   StepExecution: " + contribution.getStepExecution());
            return RepeatStatus.FINISHED;
        };
    }
}
