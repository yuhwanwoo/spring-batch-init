package com.example.springbatch.jobrepository;

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
public class JobRepositoryConfiguration {

    private final JobRepositoryListener jobRepositoryListener;

    @Bean
    public Job jobRepositoryJob(JobRepository jobRepository,
                                Step jobRepositoryStep1,
                                Step jobRepositoryStep2) {
        return new JobBuilder("jobRepositoryJob", jobRepository)
                .start(jobRepositoryStep1)
                .next(jobRepositoryStep2)
                .listener(jobRepositoryListener) // Listener 등록
                .build();
    }

    @Bean
    public Step jobRepositoryStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobRepositoryStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step jobRepositoryStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobRepositoryStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 기존 코드의 return null; 의도 유지 (null 반환 시에도 Step은 종료됨)
                    return null;
                }, transactionManager)
                .build();
    }
}
