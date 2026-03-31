package com.example.springbatch.delimetedlinetokenizer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class FlatFilesDelimitedConfiguration {

    @Bean
    public Job flatFilesDelimitedJob(JobRepository jobRepository, 
                                     Step flatFilesDelimitedStep1, 
                                     Step flatFilesDelimitedStep2) {
        return new JobBuilder("flatFilesDelimitedJob", jobRepository)
                .start(flatFilesDelimitedStep1)
                .next(flatFilesDelimitedStep2)
                .build();
    }

    @Bean
    public Step flatFilesDelimitedStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flatFilesDelimitedStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가 및 다루는 타입을 Customer로 수정
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(flatFilesDelimitedItemReader())
                .writer(new ItemWriter<Customer>() {
                    @Override
                    // Spring Batch 5: List -> Chunk 타입 변경
                    public void write(Chunk<? extends Customer> chunk) throws Exception {
                        chunk.getItems().forEach(item -> System.out.println(item));
                    }
                })
                .build();
    }

    @Bean
    public FlatFileItemReader<Customer> flatFilesDelimitedItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("flatFile")
                .resource(new ClassPathResource("customer.csv"))
                .fieldSetMapper(new CustomerFieldSetMapper())
//              .targetType(Customer.class)
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("name","year","age")
                .build();
    }

    @Bean
    public FlatFileItemReader<Customer> flatFilesDelimitedItemReader2() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("flatFile")
                .resource(new ClassPathResource("customer.csv"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .targetType(Customer.class)
                .linesToSkip(1)
                .delimited().delimiter(",")
                .names("name","year","age")
                .build();
    }

    @Bean
    public Step flatFilesDelimitedStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flatFilesDelimitedStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}