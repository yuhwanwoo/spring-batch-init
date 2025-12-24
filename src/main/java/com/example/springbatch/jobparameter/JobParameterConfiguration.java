package com.example.springbatch.jobparameter;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Date;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class JobParameterConfiguration {

    @Bean
    public Job parameterJob(JobRepository jobRepository, Step parameterStep1, Step parameterStep2) {
        return new JobBuilder("parameterJob", jobRepository)
                .start(parameterStep1)
                .next(parameterStep2)
                .build();
    }

    @Bean
    public Step parameterStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("parameterStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    // JobParameters 가져오기
                    JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
                    String name = jobParameters.getString("name");
                    long seq = jobParameters.getLong("seq", 0L); // null safe 처리
                    Date date = jobParameters.getDate("date");

                    System.out.println("===========================");
                    System.out.println("name: " + name);
                    System.out.println("seq: " + seq);
                    System.out.println("date: " + date);
                    System.out.println("===========================");

                    // ChunkContext를 통한 접근 (Map 활용)
                    Map<String, Object> jobParametersMap = chunkContext.getStepContext().getJobParameters();

                    System.out.println("parameterStep1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager) // TransactionManager 명시 필수
                .build();
    }

    @Bean
    public Step parameterStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("parameterStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("parameterStep2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
