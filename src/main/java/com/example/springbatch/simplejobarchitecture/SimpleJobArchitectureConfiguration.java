package com.example.springbatch.simplejobarchitecture;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class SimpleJobArchitectureConfiguration {

    @Bean
    public Job simpleJobArchitectureJob(JobRepository jobRepository, 
                                        Step simpleJobArchitectureStep1, 
                                        Step simpleJobArchitectureStep2) {
        return new JobBuilder("simpleJobArchitectureJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(simpleJobArchitectureStep1)
                .next(simpleJobArchitectureStep2)
                .listener(new CustomJobListener()) // 리스너 설정 유지
                .build();
    }

    @Bean
    public Step simpleJobArchitectureStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleJobArchitectureStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step simpleJobArchitectureStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleJobArchitectureStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}