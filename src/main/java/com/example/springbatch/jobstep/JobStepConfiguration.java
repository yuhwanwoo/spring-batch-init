package com.example.springbatch.jobstep;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class JobStepConfiguration {

    @Bean
    public Job jobStepConfigParentJob(JobRepository jobRepository,
                                      Step jobStepConfigJobStep,
                                      Step jobStepConfigStep2) {
        return new JobBuilder("jobStepConfigParentJob", jobRepository)
                .start(jobStepConfigJobStep) // 자식 Job을 실행하는 Step
                .next(jobStepConfigStep2)
                .build();
    }

    @Bean
    public Job jobStepConfigChildJob(JobRepository jobRepository, Step jobStepConfigStep1) {
        return new JobBuilder("jobStepConfigChildJob", jobRepository)
                .start(jobStepConfigStep1)
                .build();
    }

    @Bean
    public Step jobStepConfigJobStep(JobRepository jobRepository,
                                     JobLauncher jobLauncher,
                                     Job jobStepConfigChildJob,
                                     DefaultJobParametersExtractor jobStepConfigJobParametersExtractor) {
        
        return new StepBuilder("jobStepConfigJobStep", jobRepository)
                .job(jobStepConfigChildJob)       // 실행할 자식 Job 지정
                .launcher(jobLauncher)            // JobLauncher 필수
                .parametersExtractor(jobStepConfigJobParametersExtractor) // 파라미터 추출기
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        // 1. 부모 Step의 ExecutionContext에 "name" 저장
                        stepExecution.getExecutionContext().putString("name", "user1");
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        return null;
                    }
                })
                .build();
    }

    @Bean
    public Step jobStepConfigStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobStepConfigStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // Child Job이 실행하는 Step
                    System.out.println(">> step1 (Child Job) has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step jobStepConfigStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jobStepConfigStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step2 (Parent Job) has executed");
                    
                    // 기존 로직 유지: 예외 발생 시킴
                    throw new RuntimeException("step2 was failed");
                    // return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public DefaultJobParametersExtractor jobStepConfigJobParametersExtractor() {
        DefaultJobParametersExtractor extractor = new DefaultJobParametersExtractor();
        // 2. ExecutionContext에 있는 "name" 키를 찾아 자식 Job의 파라미터로 전달
        extractor.setKeys(new String[]{"name"});
        return extractor;
    }
}