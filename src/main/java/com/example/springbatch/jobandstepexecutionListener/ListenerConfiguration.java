package com.example.springbatch.jobandstepexecutionListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ListenerConfiguration {

    private final DataSource dataSource;

    @Bean
    public Job listenerJob(JobRepository jobRepository, Step listenerStep1, Step listenerStep2) {
        return new JobBuilder("listenerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(listenerStep1)
                .next(listenerStep2)
                .build();
    }

    @Bean
    public Step listenerStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("listenerStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">> listenerStep1 has been executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager) // Spring Batch 5: 트랜잭션 매니저 필수
                .listener(new CustomStepListener()) // 인터페이스 구현 방식 리스너
                .build();
    }

    @Bean
    public Step listenerStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("listenerStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">> listenerStep2 has been executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(new AnnotationCustomStepListener()) // 애노테이션 방식 리스너
                .build();
    }
}