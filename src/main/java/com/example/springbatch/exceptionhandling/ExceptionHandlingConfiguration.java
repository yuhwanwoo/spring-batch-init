package com.example.springbatch.exceptionhandling;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ExceptionHandlingConfiguration {

    @Bean
    public Job exceptionHandlingJob(JobRepository jobRepository, 
                                    Step exceptionHandlingStep1, 
                                    Step exceptionHandlingStep2) {
        return new JobBuilder("exceptionHandlingJob", jobRepository)
                .start(exceptionHandlingStep1)
                .next(exceptionHandlingStep2)
                .build();
    }

    @Bean
    public Step exceptionHandlingStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("exceptionHandlingStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 및 제네릭을 Customer로 수정
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(exceptionHandlingItemReader())
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
    public FlatFileItemReader<Customer> exceptionHandlingItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("flatFile")
                .resource(new ClassPathResource("customer.txt"))
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>())
                .targetType(Customer.class)
                .linesToSkip(1)
                .fixedLength()
                .strict(false)
                .addColumns(new Range(1, 5))
                .addColumns(new Range(6, 9))
                .addColumns(new Range(10, 11))
                .names("name", "year", "age")
                .build();
    }

    @Bean
    public Step exceptionHandlingStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("exceptionHandlingStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}