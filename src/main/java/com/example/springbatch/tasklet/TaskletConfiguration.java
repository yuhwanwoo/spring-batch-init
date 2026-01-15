package com.example.springbatch.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class TaskletConfiguration {

    // CustomTasklet이 빈으로 등록되어 있어야 합니다 (@Component 등)
    private final CustomTasklet customTasklet;

    @Bean
    public Job taskletJob(JobRepository jobRepository, Step taskletStep1, Step taskletStep2) {
        return new JobBuilder("taskletJob", jobRepository)
                .start(taskletStep1)
                .next(taskletStep2)
                .build();
    }

    @Bean
    public Step taskletStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStep1", jobRepository)
                .tasklet((stepContribution, chunkContext) -> {
                    // 익명 클래스를 람다식으로 간소화
                    System.out.println("stepContribution = " + stepContribution + ", chunkContext = " + chunkContext);
                    return RepeatStatus.FINISHED;
                }, transactionManager) // TransactionManager 명시
                .build();
    }

    @Bean
    public Step taskletStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStep2", jobRepository)
                .tasklet(customTasklet, transactionManager) // 주입받은 커스텀 Tasklet 사용
                .build();
    }
}