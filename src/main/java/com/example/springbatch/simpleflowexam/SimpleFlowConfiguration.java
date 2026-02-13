package com.example.springbatch.simpleflowexam;

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
    public Job simpleFlowBatchJob(JobRepository jobRepository,
                                  Flow simpleFlowFlow1,
                                  Flow simpleFlowFlow2,
                                  Flow simpleFlowFlow3) {
        return new JobBuilder("simpleFlowBatchJob", jobRepository)
                .start(simpleFlowFlow1)
                    .on("COMPLETED").to(simpleFlowFlow2) // Flow1 성공 시 Flow2 실행
                .from(simpleFlowFlow1)
                    .on("FAILED").to(simpleFlowFlow3)    // Flow1 실패 시 Flow3 실행
                .end()
                .build();
    }

    // --- Flows ---

    @Bean
    public Flow simpleFlowFlow1(Step simpleFlowStep1, Step simpleFlowStep2) {
        return new FlowBuilder<Flow>("simpleFlowFlow1")
                .start(simpleFlowStep1)
                .next(simpleFlowStep2)
                .end();
    }

    @Bean
    public Flow simpleFlowFlow2(Flow simpleFlowFlow3, Step simpleFlowStep5, Step simpleFlowStep6) {
        // Flow2는 내부적으로 Flow3를 포함하고 있음 (Nested Flow)
        return new FlowBuilder<Flow>("simpleFlowFlow2")
                .start(simpleFlowFlow3) // Flow3 실행
                .next(simpleFlowStep5)
                .next(simpleFlowStep6)
                .end();
    }

    @Bean
    public Flow simpleFlowFlow3(Step simpleFlowStep3, Step simpleFlowStep4) {
        return new FlowBuilder<Flow>("simpleFlowFlow3")
                .start(simpleFlowStep3)
                .next(simpleFlowStep4)
                .end();
    }

    // --- Steps ---

    @Bean
    public Step simpleFlowStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    // 테스트: 주석 해제 시 FAILED 처리되어 Flow3로 이동
                    // throw new RuntimeException("step1 was failed");
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

    @Bean
    public Step simpleFlowStep4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step4 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step simpleFlowStep5(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep5", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step5 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step simpleFlowStep6(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep6", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step6 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}