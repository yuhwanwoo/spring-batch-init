package com.example.springbatch.jobscopestepscope;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class JobScope_StepScope_Configuration {

    @Bean
    public Job jobScopeStepScopeBatchJob(JobRepository jobRepository, 
                                         Step jobScopeStepScopeStep1, 
                                         Step jobScopeStepScopeStep2) {
        return new JobBuilder("jobScopeStepScopeBatchJob", jobRepository)
                .start(jobScopeStepScopeStep1)
                .next(jobScopeStepScopeStep2)
                .listener(new JobListener()) // 커스텀 리스너 (아래 정의됨)
                .build();
    }

    @Bean
    @JobScope
    public Step jobScopeStepScopeStep1(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       // @JobScope 덕분에 JobParameters 값 주입 가능 (Job 실행 시점 생성)
                                       @Value("#{jobParameters['message']}") String message,
                                       // StepScope Tasklet 주입
                                       Tasklet jobScopeStepScopeTasklet1) {
        
        System.out.println("jobParameters['message'] : " + message);

        return new StepBuilder("jobScopeStepScopeStep1", jobRepository)
                .tasklet(jobScopeStepScopeTasklet1, transactionManager)
                .build();
    }

    @Bean
    public Step jobScopeStepScopeStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobScopeStepScopeStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet jobScopeStepScopeTasklet1(@Value("#{jobExecutionContext['name']}") String name) {
        return (stepContribution, chunkContext) -> {
            // @StepScope 덕분에 Step 실행 시점의 ExecutionContext 값 주입 가능
            System.out.println("jobExecutionContext['name'] : " + name);
            return RepeatStatus.FINISHED;
        };
    }

    // 예제를 돕기 위한 JobListener 구현체 (Job 실행 전 Context에 값 세팅)
    public static class JobListener implements JobExecutionListener {
        @Override
        public void beforeJob(JobExecution jobExecution) {
            jobExecution.getExecutionContext().putString("name", "user1");
        }
    }
}