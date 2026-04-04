package com.example.springbatch.jsonitemreader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class JsonConfiguration {

    @Bean
    public Job jsonConfigurationJob(JobRepository jobRepository, Step jsonConfigurationStep1) {
        return new JobBuilder("jsonConfigurationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(jsonConfigurationStep1)
                .build();
    }

    @Bean
    public Step jsonConfigurationStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jsonConfigurationStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(jsonConfigurationItemReader())
                .writer(jsonConfigurationItemWriter())
                .build();
    }

    @Bean
    public JsonItemReader<Customer> jsonConfigurationItemReader() {
        return new JsonItemReaderBuilder<Customer>()
                .jsonObjectReader(new JacksonJsonObjectReader<>(Customer.class))
                .resource(new ClassPathResource("customer.json"))
                .name("jsonItemReader")
                .build();
    }

    @Bean
    public ItemWriter<Customer> jsonConfigurationItemWriter() {
        // Spring Batch 5: 람다식 파라미터가 List에서 Chunk로 변경됨
        return chunk -> {
            for (Customer item : chunk.getItems()) {
                System.out.println(item.toString());
            }
        };
    }
}