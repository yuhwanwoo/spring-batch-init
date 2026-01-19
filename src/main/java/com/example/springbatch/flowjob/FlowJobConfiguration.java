package com.example.springbatch.flowjob;

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
public class FlowJobConfiguration {

    @Bean
    public Job flowJobBatchJob(JobRepository jobRepository, 
                               Step flowJobStep1, 
                               Step flowJobStep2, 
                               Step flowJobStep3) {
        return new JobBuilder("flowJobBatchJob", jobRepository)
                .start(flowJobStep1)
                    .on("COMPLETED").to(flowJobStep2) // Step1 성공(COMPLETED) 시 Step2 이동
                .from(flowJobStep1) // Step1의 결과에 대한 분기로 다시 돌아옴
                    .on("FAILED").to(flowJobStep3)    // Step1 실패(FAILED) 시 Step3 이동
                .end() // Flow 종료
                .build();
    }

    @Bean
    public Step flowJobStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    
                    // 주석 해제 시 FAILED 상태가 되어 step3으로 이동함
                    // throw new RuntimeException("Intentional Failure");
                    
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step flowJobStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowJobStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step flowJobStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowJobStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}