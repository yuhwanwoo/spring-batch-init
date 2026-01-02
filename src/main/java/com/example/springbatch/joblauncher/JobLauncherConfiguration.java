package com.example.springbatch.joblauncher;

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
public class JobLauncherConfiguration {

    @Bean
    public Job launcherJob(JobRepository jobRepository, Step launcherStep1, Step launcherStep2) {
        return new JobBuilder("launcherJob", jobRepository)
                .start(launcherStep1)
                .next(launcherStep2)
                .incrementer(new RunIdIncrementer()) // RunIdIncrementer 적용
                .build();
    }

    @Bean
    public Step launcherStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("launcherStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 기존 로직: 3초 대기
                    Thread.sleep(3000);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step launcherStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("launcherStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 기존 로직: null 반환 (종료 처리됨)
                    return null;
                }, transactionManager)
                .build();
    }
}
