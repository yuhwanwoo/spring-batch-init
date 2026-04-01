package com.example.springbatch.fixedlengthtokenizer;

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
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class FlatFilesFixedLengthConfiguration {

    @Bean
    public Job flatFilesFixedLengthJob(JobRepository jobRepository, 
                                       Step flatFilesFixedLengthStep1, 
                                       Step flatFilesFixedLengthStep2) {
        return new JobBuilder("flatFilesFixedLengthJob", jobRepository)
                .start(flatFilesFixedLengthStep1)
                .next(flatFilesFixedLengthStep2)
                .build();
    }

    @Bean
    public Step flatFilesFixedLengthStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flatFilesFixedLengthStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 추가 및 실제 타입인 Customer로 수정
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(flatFilesFixedLengthItemReader())
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
    public FlatFileItemReader<Customer> flatFilesFixedLengthItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("flatFile")
                .resource(new FileSystemResource("C:\\lecture\\src\\main\\resources\\customer.txt"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .targetType(Customer.class)
                .linesToSkip(1)
                .fixedLength()
                .addColumns(new Range(1,5))
                .addColumns(new Range(6,9))
                .addColumns(new Range(10,11))
                /*
                .addColumns(new Range(1))
                .addColumns(new Range(6))
                .addColumns(new Range(10))
                */
                .names("name","year","age")
                .build();
    }

    @Bean
    public Step flatFilesFixedLengthStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flatFilesFixedLengthStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}