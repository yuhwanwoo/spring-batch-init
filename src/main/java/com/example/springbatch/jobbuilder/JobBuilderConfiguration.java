package com.example.springbatch.jobbuilder;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class JobBuilderConfiguration {

    @Bean
    public Job batchJob1(JobRepository jobRepository, Step jobBuilderStep1, Step jobBuilderStep2) {
        return new JobBuilder("batchJob1", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(jobBuilderStep1)
                .next(jobBuilderStep2)
                .build();
    }

    @Bean
    public Job batchJob2(JobRepository jobRepository, Flow jobBuilderFlow, Step jobBuilderStep2) {
        return new JobBuilder("batchJob2", jobRepository) // 원본의 이름 중복("batchJob1") 수정
                .incrementer(new RunIdIncrementer())
                .start(jobBuilderFlow)
                .next(jobBuilderStep2)
                .end() // Flow를 포함한 Job 구성 시 end() 호출
                .build();
    }

    @Bean
    public Flow jobBuilderFlow(Step jobBuilderStep3, Step jobBuilderStep4) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("jobBuilderFlow");
        flowBuilder.start(jobBuilderStep3)
                .next(jobBuilderStep4)
                .end();
        return flowBuilder.build();
    }

    // --- Steps ---

    @Bean
    public Step jobBuilderStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobBuilderStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step jobBuilderStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobBuilderStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step jobBuilderStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobBuilderStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step jobBuilderStep4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobBuilderStep4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step4 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}