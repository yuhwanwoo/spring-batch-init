package com.example.springbatch.retry.api;

import com.example.springbatch.retry.NoRetryException;
import com.example.springbatch.retry.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class RetryConfiguration {

    @Bean
    public Job retryJob(JobRepository jobRepository, Step retryStep1) throws Exception {
        return new JobBuilder("retryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(retryStep1)
                .build();
    }

    @Bean
    public Step retryStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("retryStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<String, Customer>chunk(5, transactionManager)
                .reader(retryReader())
                .processor(retryProcessor())
                .writer(retryWriter())
                .faultTolerant()
//              .skip(RetryableException.class)
//              .skipLimit(2)
                .retry(RetryableException.class)
                .noRetry(NoRetryException.class)
                .retryLimit(2)
                .build();
    }

    @Bean
    public SimpleRetryPolicy retryLimitCheckingItemSkipPolicy() {

        Map<Class<? extends Throwable>, Boolean> exceptionClass = new HashMap<>();
        exceptionClass.put(RetryableException.class, true);

        return new SimpleRetryPolicy(2, exceptionClass);
    }

    @Bean
    public ListItemReader<String> retryReader() {

        List<String> items = new ArrayList<>();

        for(int i = 0; i < 30; i++) {
            items.add(String.valueOf(i));
        }

        return new ListItemReader<>(items);
    }

    @Bean
    public ItemProcessor<String, Customer> retryProcessor() {
        // 제네릭 타입을 명시하여 원시 타입 경고 제거
        return new RetryItemProcessor();
    }

    @Bean
    public ItemWriter<Customer> retryWriter() {
        // 제네릭 타입을 명시하여 원시 타입 경고 제거
        return new RetryItemWriter();
    }
}