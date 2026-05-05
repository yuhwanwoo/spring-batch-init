package com.example.springbatch.multithreadedstep;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class MultiThreadStepConfiguration {

    private final DataSource dataSource;

    @Bean
    public Job multiThreadJob(JobRepository jobRepository, Step multiThreadStep1) {
        return new JobBuilder("multiThreadJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(multiThreadStep1)
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step multiThreadStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("multiThreadStep1", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저 필수
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(multiThreadPagingItemReader())
                .listener(new CustomReadListener())
                .processor((ItemProcessor<Customer, Customer>) item -> item)
                .listener(new CustomProcessListener())
                .writer(multiThreadCustomItemWriter())
                .listener(new CustomWriteListener())
                // 멀티 스레드 수행을 위한 TaskExecutor 설정
                .taskExecutor(multiThreadTaskExecutor())
                .throttleLimit(8) // taskExecutor의 최대 스레드 수와 맞추는 것이 효율적입니다.
                .build();
    }

    /**
     * Paging 방식은 멀티 스레드 환경에서 Thread-safe 합니다.
     */
    @Bean
    public JdbcPagingItemReader<Customer> multiThreadPagingItemReader() {
        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("multiThreadPagingItemReader")
                .dataSource(dataSource)
                .pageSize(100)
                .queryProvider(queryProvider)
                .rowMapper(new CustomerRowMapper())
                .build();
    }

    /**
     * Cursor 방식은 기본적으로 Thread-safe 하지 않습니다. 
     * 멀티 스레드에서 사용하려면 별도의 동기화 처리가 필요합니다.
     */
    @Bean
    public JdbcCursorItemReader<Customer> multiThreadCustomItemReader() {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("multiThreadJdbcCursorItemReader")
                .fetchSize(100)
                .sql("select id, firstName, lastName, birthdate from customer order by id")
                .beanRowMapper(Customer.class)
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> multiThreadCustomItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @Bean
    public TaskExecutor multiThreadTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("multi-thread-");
        executor.initialize(); // 빈 생성을 위해 초기화 필수
        return executor;
    }
}