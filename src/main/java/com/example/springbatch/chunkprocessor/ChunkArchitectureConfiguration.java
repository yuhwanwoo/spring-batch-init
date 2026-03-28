package com.example.springbatch.chunkprocessor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
public class ChunkArchitectureConfiguration {

    @Bean
    public Job chunkArchitectureJob(JobRepository jobRepository, 
                                    Step chunkArchitectureStep1, 
                                    Step chunkArchitectureStep2) {
        return new JobBuilder("chunkArchitectureJob", jobRepository)
                .start(chunkArchitectureStep1)
                .next(chunkArchitectureStep2)
                .build();
    }

    @Bean
    public Step chunkArchitectureStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkArchitectureStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가 및 제네릭을 Customer 타입으로 수정
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(chunkArchitectureItemReader())
                .processor(chunkArchitectureItemProcessor())
                .writer(chunkArchitectureItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer> chunkArchitectureItemReader() {
        return new CustomItemReader(Arrays.asList(new Customer("user1"), new Customer("user2"), new Customer("user3")));
    }

    @Bean
    public ItemProcessor<Customer, Customer> chunkArchitectureItemProcessor() {
        return new CustomItemProcessor();
    }

    @Bean
    public ItemWriter<Customer> chunkArchitectureItemWriter() {
        return new CustomItemWriter();
    }

    @Bean
    public Step chunkArchitectureStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkArchitectureStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}