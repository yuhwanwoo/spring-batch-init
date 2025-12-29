package com.example.springbatch.stepcontrbution;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
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
public class StepContributionConfiguration {

    @Bean
    public Job contributionJob(JobRepository jobRepository, Step contributionStep1, Step contributionStep2) {
        return new JobBuilder("contributionJob", jobRepository)
                .start(contributionStep1)
                .next(contributionStep2)
                .build();
    }

    @Bean
    public Step contributionStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("contributionStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    // StepContribution 정보 출력
                    System.out.println("contribution.getExitStatus(): " + contribution.getExitStatus());
                    System.out.println("contribution.getStepExecution().getStepName(): " + contribution.getStepExecution().getStepName());
                    System.out.println("contribution.getStepExecution().getJobExecution().getJobInstance().getJobName(): " + contribution.getStepExecution().getJobExecution().getJobInstance().getJobName());

                    // 상태를 강제로 STOPPED로 변경
                    // (이 설정으로 인해 뒤에 연결된 next(step2)는 실행되지 않고 Job이 멈추게 됩니다)
                    contribution.setExitStatus(ExitStatus.STOPPED);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step contributionStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("contributionStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("contributionStep2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}