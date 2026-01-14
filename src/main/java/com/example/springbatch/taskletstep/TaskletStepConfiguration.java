package com.example.springbatch.taskletstep;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
public class TaskletStepConfiguration {

    @Bean
    public Job taskletStepBatchJob(JobRepository jobRepository, 
                                   Step taskletStepTaskStep, 
                                   Step taskletStepChunkStep) {
        return new JobBuilder("taskletStepBatchJob", jobRepository)
                .start(taskletStepTaskStep)
                .next(taskletStepChunkStep)
                .build();
    }

    @Bean
    public Step taskletStepTaskStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStepTaskStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step taskletStepChunkStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("taskletStepChunkStep", jobRepository)
                // Chunk 설정 시 TransactionManager 필수
                .<String, String>chunk(3, transactionManager) 
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3"))) // 타입 명시 권장 (<>)
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        return item.toUpperCase();
                    }
                })
                .writer(list -> {
                    list.forEach(item -> System.out.println(item));
                })
                .build();
    }
}