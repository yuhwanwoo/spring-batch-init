package com.example.springbatch.itemreader;

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
public class ItemReader_ItemProcessor_ItemWriter_Configuration {

    @Bean
    public Job itemReaderProcessorWriterJob(JobRepository jobRepository, 
                                            Step itemReaderProcessorWriterStep1, 
                                            Step itemReaderProcessorWriterStep2) {
        return new JobBuilder("itemReaderProcessorWriterJob", jobRepository)
                .start(itemReaderProcessorWriterStep1)
                .next(itemReaderProcessorWriterStep2)
                .build();
    }

    @Bean
    public Step itemReaderProcessorWriterStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("itemReaderProcessorWriterStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가 및 Customer 타입으로 제네릭 수정
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(itemReaderProcessorWriterReader())
                .processor(itemReaderProcessorWriterProcessor())
                .writer(itemReaderProcessorWriterWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer> itemReaderProcessorWriterReader() {
        return new CustomItemReader(Arrays.asList(new Customer("user1"), new Customer("user2"), new Customer("user3")));
    }

    @Bean
    public ItemProcessor<Customer, Customer> itemReaderProcessorWriterProcessor() {
        return new CustomItemProcessor();
    }

    @Bean
    public ItemWriter<Customer> itemReaderProcessorWriterWriter() {
        return new CustomItemWriter();
    }

    @Bean
    public Step itemReaderProcessorWriterStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("itemReaderProcessorWriterStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}