package com.example.springbatch.flatfileitemreder;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class FlatFilesConfiguration {

    @Bean
    public Job flatFilesJob(JobRepository jobRepository, 
                            Step flatFilesStep1, 
                            Step flatFilesStep2) {
        return new JobBuilder("flatFilesJob", jobRepository)
                .start(flatFilesStep1)
                .next(flatFilesStep2)
                .build();
    }

    @Bean
    public Step flatFilesStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flatFilesStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가 및 제네릭을 Customer 타입으로 수정
                .<Customer, Customer>chunk(5, transactionManager)
                .reader(flatFilesItemReader())
                .writer(new ItemWriter<Customer>() {
                    @Override
                    // Spring Batch 5: List -> Chunk 타입 변경
                    public void write(Chunk<? extends Customer> chunk) throws Exception {
                        System.out.println("items = " + chunk.getItems());
                    }
                })
                .build();
    }

    @Bean
    public Step flatFilesStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flatFilesStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public FlatFileItemReader<Customer> flatFilesItemReader(){

        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("/customer.csv"));

        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer());
        // CustomerFieldSetMapper 클래스는 기존에 구현하신 내용을 그대로 사용하시면 됩니다.
        lineMapper.setFieldSetMapper(new CustomerFieldSetMapper());

        itemReader.setLineMapper(lineMapper);
        itemReader.setLinesToSkip(1);

        return itemReader;
    }
}