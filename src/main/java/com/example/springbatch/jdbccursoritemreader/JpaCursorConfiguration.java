package com.example.springbatch.jdbccursoritemreader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

// Spring Boot 3.x (Hibernate 6) 이상에서는 javax 대신 jakarta를 사용합니다.
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;

@RequiredArgsConstructor
@Configuration
public class JpaCursorConfiguration {

    // Spring Batch 5: Factory 대신 EntityManagerFactory만 주입받습니다.
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job jpaCursorJob(JobRepository jobRepository, Step jpaCursorStep1) {
        return new JobBuilder("jpaCursorJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(jpaCursorStep1)
                .build();
    }

    @Bean
    public Step jpaCursorStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jpaCursorStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<Customer, Customer>chunk(2, transactionManager)
                .reader(jpaCursorItemReader())
                .writer(jpaCursorItemWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Customer> jpaCursorItemReader() {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "A%");

        // Builder에 <Customer> 제네릭 타입 명시
        return new JpaCursorItemReaderBuilder<Customer>()
                .name("jpaCursorItemReader")
                .queryString("select c from Customer c where firstname like :firstname")
                .entityManagerFactory(entityManagerFactory)
                .parameterValues(parameters)
//              .maxItemCount(10)
//              .currentItemCount(2)
                .build();
    }

    @Bean
    public ItemWriter<Customer> jpaCursorItemWriter() {
        // Spring Batch 5: 람다식 파라미터가 List에서 Chunk로 변경됨
        return chunk -> {
            for (Customer item : chunk.getItems()) {
                System.out.println(item.toString());
            }
        };
    }
}