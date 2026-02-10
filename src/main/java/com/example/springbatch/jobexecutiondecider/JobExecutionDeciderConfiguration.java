package com.example.springbatch.jobexecutiondecider;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class JobExecutionDeciderConfiguration {

    @Bean
    public Job deciderBatchJob(JobRepository jobRepository,
                               Step deciderStartStep,
                               Step deciderEvenStep,
                               Step deciderOddStep,
                               JobExecutionDecider decider) {
        return new JobBuilder("deciderBatchJob", jobRepository)
                .start(deciderStartStep)
                .next(decider) // Decider 실행
                .from(decider) // Decider의 결과에 따른 분기
                    .on("ODD").to(deciderOddStep)
                .from(decider)
                    .on("EVEN").to(deciderEvenStep)
                .end() // Flow 종료
                .build();
    }

    @Bean
    public Step deciderStartStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deciderStartStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("This is the start tasklet");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step deciderEvenStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deciderEvenStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">>EvenStep has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step deciderOddStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("deciderOddStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">>OddStep has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new CustomDecider();
    }

    // Decider 클래스 (기존 로직 유지)
    public static class CustomDecider implements JobExecutionDecider {

        private int count = 0;

        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            count++;
            System.out.println("Current count: " + count);

            if (count % 2 == 0) {
                return new FlowExecutionStatus("EVEN");
            } else {
                return new FlowExecutionStatus("ODD");
            }
        }
    }
}