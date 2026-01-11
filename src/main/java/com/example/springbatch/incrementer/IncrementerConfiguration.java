package com.example.springbatch.incrementer;

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
public class IncrementerConfiguration {

    @Bean
    public Job incrementerJob(JobRepository jobRepository, 
                              Step incrementerStep1, 
                              Step incrementerStep2, 
                              Step incrementerStep3) {
        return new JobBuilder("incrementerJob", jobRepository)
                /*
                 * Incrementer 설정:
                 * 기존 코드의 의도대로 CustomJobParametersIncrementer 적용
                 * (RunIdIncrementer는 주석 처리 유지)
                 */
//              .incrementer(new RunIdIncrementer())
                .incrementer(new CustomJobParametersIncrementer()) 
                .start(incrementerStep1)
                .next(incrementerStep2)
                .next(incrementerStep3)
                .build();
    }

    @Bean
    public Step incrementerStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("incrementerStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step incrementerStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("incrementerStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step incrementerStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("incrementerStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}