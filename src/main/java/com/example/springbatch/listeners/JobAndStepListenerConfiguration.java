package com.example.springbatch.listeners;

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

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JobAndStepListenerConfiguration {

    // 외부에서 주입받는 커스텀 스텝 리스너 빈
    private final CustomStepListener customStepListener;

    @Bean
    public Job jobAndStepListenerJob(JobRepository jobRepository, 
                                     Step jobAndStepListenerStep1, 
                                     Step jobAndStepListenerStep2) {
        return new JobBuilder("jobAndStepListenerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(jobAndStepListenerStep1)
                .next(jobAndStepListenerStep2)
                .listener(new CustomJobListener()) // Job 전용 리스너 등록
                .build();
    }

    @Bean
    public Step jobAndStepListenerStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobAndStepListenerStep1", jobRepository)
                // Spring Batch 5: Tasklet 구현 시 transactionManager 전달이 필수입니다.
                .tasklet((contribution, chunkContext) -> {
                    log.info(">> jobAndStepListenerStep1 executed");
                    // 필요 시 주석을 해제하여 실패 테스트 가능
                    // throw new RuntimeException("failed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(customStepListener) // Step 리스너 등록
                .build();
    }

    @Bean
    public Step jobAndStepListenerStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobAndStepListenerStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">> jobAndStepListenerStep2 executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(customStepListener) // 동일한 Step 리스너 공유 등록
                .build();
    }
}