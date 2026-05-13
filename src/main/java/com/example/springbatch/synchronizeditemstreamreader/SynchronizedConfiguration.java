package com.example.springbatch.synchronizeditemstreamreader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SynchronizedConfiguration {

    private final DataSource dataSource;

    @Bean
    public Job synchronizedJob(JobRepository jobRepository, Step synchronizedStep1) {
        return new JobBuilder("synchronizedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(synchronizedStep1)
                .build();
    }

    @Bean
    public Step synchronizedStep1(JobRepository jobRepository, 
                                  PlatformTransactionManager transactionManager,
                                  SynchronizedItemStreamReader<Customer> synchronizedCustomItemReader,
                                  JdbcBatchItemWriter<Customer> synchronizedCustomerItemWriter) {
        return new StepBuilder("synchronizedStep1", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저 필수
                .<Customer, Customer>chunk(60, transactionManager)
                .reader(synchronizedCustomItemReader)
                .listener(new ItemReadListener<Customer>() {
                    @Override
                    public void afterRead(Customer item) {
                        System.out.println("Thread: " + Thread.currentThread().getName() + " | item.getId() : " + item.getId());
                    }
                })
                .writer(synchronizedCustomerItemWriter)
                // 멀티 스레드 설정 (SynchronizedReader 덕분에 안전함)
                .taskExecutor(synchronizedTaskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<Customer> synchronizedCustomItemReader() {
        // 1. Thread-safe 하지 않은 Cursor 리더 생성
        JdbcCursorItemReader<Customer> notSafetyReader = new JdbcCursorItemReaderBuilder<Customer>()
                .fetchSize(60)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .sql("select id, firstName, lastName, birthdate from customer")
                .name("NotSafetyReader")
                .build();

        // 2. 동기화 처리를 위한 SynchronizedItemStreamReader로 래핑
        return new SynchronizedItemStreamReaderBuilder<Customer>()
                .delegate(notSafetyReader)
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> synchronizedCustomerItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @Bean
    public TaskExecutor synchronizedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("synchronized-thread-");
        executor.initialize(); // 초기화 필수
        return executor;
    }
}