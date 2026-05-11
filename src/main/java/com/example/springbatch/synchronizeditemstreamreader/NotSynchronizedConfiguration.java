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
public class NotSynchronizedConfiguration {

    private final DataSource dataSource;

    @Bean
    public Job notSynchronizedJob(JobRepository jobRepository, Step notSynchronizedStep1) {
        return new JobBuilder("notSynchronizedJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(notSynchronizedStep1)
                .build();
    }

    @Bean
    public Step notSynchronizedStep1(JobRepository jobRepository, 
                                     PlatformTransactionManager transactionManager,
                                     JdbcCursorItemReader<Customer> notSynchronizedCustomItemReader,
                                     JdbcBatchItemWriter<Customer> notSynchronizedCustomerItemWriter) {
        return new StepBuilder("notSynchronizedStep1", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저 필수
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(notSynchronizedCustomItemReader)
                .listener(new ItemReadListener<Customer>() {
                    @Override
                    public void afterRead(Customer item) {
                        System.out.println("Thread: " + Thread.currentThread().getName() + " | item.getId() : " + item.getId());
                    }
                })
                .writer(notSynchronizedCustomerItemWriter)
                // 멀티 스레드 설정 (동기화되지 않은 Reader와 함께 사용 시 위험함)
                .taskExecutor(notSynchronizedTaskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Customer> notSynchronizedCustomItemReader() {
        return new JdbcCursorItemReaderBuilder<Customer>()
                .name("NotSafetyReader")
                .fetchSize(100)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class))
                .sql("select id, firstName, lastName, birthdate from customer")
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> notSynchronizedCustomerItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @Bean
    public TaskExecutor notSynchronizedTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setThreadNamePrefix("not-safety-thread-");
        executor.initialize(); // 필수 초기화
        return executor;
    }
}