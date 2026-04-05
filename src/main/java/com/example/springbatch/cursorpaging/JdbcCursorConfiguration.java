package com.example.springbatch.cursorpaging;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
public class JdbcCursorConfiguration {

    // Spring Batch 5: Factory 대신 DataSource만 주입받습니다.
    private final DataSource dataSource;

    @Bean
    public Job jdbcCursorJob(JobRepository jobRepository, Step jdbcCursorStep1) {
        return new JobBuilder("jdbcCursorJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(jdbcCursorStep1)
                .build();
    }

    @Bean
    public Step jdbcCursorStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("jdbcCursorStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<Customer, Customer>chunk(10, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Customer> jdbcCursorItemReader() {
        // Builder에 <Customer> 제네릭 타입 명시
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("jdbcCursorItemReader")
                .fetchSize(10)
                .sql("select id, firstName, lastName, birthdate from customer where firstName like ? order by lastName, firstName")
                .beanRowMapper(Customer.class)
                .queryArguments("A%")
                .maxItemCount(3)
                .currentItemCount(2)
                .maxRows(100)
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ItemWriter<Customer> jdbcCursorItemWriter() {
        // Spring Batch 5: 람다식 파라미터가 List에서 Chunk로 변경됨
        return chunk -> {
            for (Customer item : chunk.getItems()) {
                System.out.println(item.toString());
            }
        };
    }
}