package com.example.springbatch.test.batch.job.api;


import com.example.springbatch.test.batch.listener.JobListener;
import com.example.springbatch.test.batch.tasklet.ApiEndTasklet;
import com.example.springbatch.test.batch.tasklet.ApiStartTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SendJobConfiguration {

    private final ApiStartTasklet apiStartTasklet;
    private final ApiEndTasklet apiEndTasklet;
    private final Step jobStep; // 외부에서 주입받는 Step

    @Bean
    public Job apiJob(JobRepository jobRepository, Step apiStep1, Step apiStep2) {
        return new JobBuilder("apiJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobListener())
                .start(apiStep1)
                .next(jobStep)
                .next(apiStep2)
                .build();
    }

    @Bean
    public Step apiStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("apiStep", jobRepository)
                .tasklet(apiStartTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step apiStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("apiStep2", jobRepository)
                .tasklet(apiEndTasklet, transactionManager)
                .build();
    }
}