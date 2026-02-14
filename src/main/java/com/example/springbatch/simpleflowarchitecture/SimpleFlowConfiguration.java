package com.example.springbatch.simpleflowarchitecture;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
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
                                  Step simpleFlowStep1, 
                                  Step simpleFlowStep2, 
                                  Flow simpleFlowFlow) {
        return new JobBuilder("simpleFlowBatchJob", jobRepository)
                .start(simpleFlowStep1)
                    .on("COMPLETED").to(simpleFlowStep2) // 성공 시 Step2 실행
                .from(simpleFlowStep1)
                    .on("FAILED").to(simpleFlowFlow)     // 실패 시 Flow 실행 (Step2 -> Step3)
                .end()
                .build();
    }

    @Bean
    public Flow simpleFlowFlow(Step simpleFlowStep2, Step simpleFlowStep3) {
        return new FlowBuilder<Flow>("simpleFlowFlow")
                .start(simpleFlowStep2)
                    .on("*").to(simpleFlowStep3) // Step2 결과와 상관없이 Step3 실행
                .end();
    }

    @Bean
    public Step simpleFlowStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleFlowStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    // 강제로 FAILED 상태로 만듦 -> Flow로 이동하게 됨
                    contribution.setExitStatus(ExitStatus.FAILED);
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