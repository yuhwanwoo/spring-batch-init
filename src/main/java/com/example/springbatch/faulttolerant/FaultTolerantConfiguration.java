package com.example.springbatch.faulttolerant;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class FaultTolerantConfiguration {

    @Bean
    public Job faultTolerantJob(JobRepository jobRepository, Step faultTolerantStep1) {
        return new JobBuilder("faultTolerantJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(faultTolerantStep1)
                .build();
    }

    @Bean
    public Step faultTolerantStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("faultTolerantStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<String, String>chunk(5, transactionManager)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() {
                        i++;
                        if(i == 1) {
                            throw new IllegalArgumentException("skip");
                        }
                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor((ItemProcessor<String, String>) item -> {
                    throw new IllegalStateException("retry");
//                  return item;
                })
                // Spring Batch 5: List -> Chunk 타입 변경
                .writer(chunk -> System.out.println(chunk.getItems()))
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(1)
                .retry(IllegalStateException.class)
                .retryLimit(2)
                .build();
    }

    @Bean
    public SimpleLimitExceptionHandler faultTolerantSimpleLimitExceptionHandler(){
        return new SimpleLimitExceptionHandler(3);
    }
}