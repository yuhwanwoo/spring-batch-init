package com.example.springbatch.parallelsteps;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ParallelStepConfiguration {

    @Bean
    public Job parallelJob(JobRepository jobRepository, Flow parallelFlow1, Flow parallelFlow2) {
        return new JobBuilder("parallelJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(parallelFlow1)
                .split(parallelTaskExecutor()) // TaskExecutor를 이용한 병렬 실행
                .add(parallelFlow2)
                .end()
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Flow parallelFlow1(Step parallelStep1) {
        return new FlowBuilder<Flow>("parallelFlow1")
                .start(parallelStep1)
                .build();
    }

    @Bean
    public Flow parallelFlow2(Step parallelStep2, Step parallelStep3) {
        return new FlowBuilder<Flow>("parallelFlow2")
                .start(parallelStep2)
                .next(parallelStep3)
                .build();
    }

    @Bean
    public Step parallelStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("parallelStep1", jobRepository)
                .tasklet(parallelTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step parallelStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("parallelStep2", jobRepository)
                .tasklet(parallelTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step parallelStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("parallelStep3", jobRepository)
                .tasklet(parallelTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet parallelTasklet() {
        return new CustomTasklet();
    }

    @Bean
    public TaskExecutor parallelTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("parallel-thread-");
        executor.initialize(); // 필수 초기화
        return executor;
    }
}