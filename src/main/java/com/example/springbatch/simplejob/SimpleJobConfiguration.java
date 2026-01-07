package com.example.springbatch.simplejob;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
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
public class SimpleJobConfiguration {

    @Bean
    public Job simpleJob(JobRepository jobRepository, 
                         Step simpleJobStep1, 
                         Step simpleJobStep2, 
                         Step simpleJobStep3) {
        return new JobBuilder("simpleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(new JobParametersValidator() {
                    @Override
                    public void validate(JobParameters parameters) throws JobParametersInvalidException {
                        // 검증 로직 없음 (기존 유지)
                    }
                })
                .preventRestart() // 재시작 방지 설정 유지
                .start(simpleJobStep1)
                .next(simpleJobStep2)
                .next(simpleJobStep3)
                .build();
    }

    @Bean
    public Step simpleJobStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step simpleJobStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleJobStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step simpleJobStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("simpleJobStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    
                    // 기존 로직 유지: 상태를 FAILED/STOPPED로 강제 변경
                    chunkContext.getStepContext().getStepExecution().setStatus(BatchStatus.FAILED);
                    contribution.setExitStatus(ExitStatus.STOPPED);
                    
                    System.out.println("step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}