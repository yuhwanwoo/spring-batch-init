package com.example.springbatch.retrylistener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class RetryListenerConfiguration {

    private final CustomRetryListener customRetryListener;

    @Bean
    public Job retryListenerJob(JobRepository jobRepository, Step retryListenerStep1) {
        return new JobBuilder("retryListenerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(retryListenerStep1)
                .build();
    }

    @Bean
    public Step retryListenerStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("retryListenerStep1", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저를 필수로 포함합니다.
                .<Integer, String>chunk(10, transactionManager)
                .reader(retryListenerListItemReader())
                .processor(new CustomItemProcessor())
                .writer(new CustomItemWriter())
                .faultTolerant()
                .retry(CustomRetryException.class)
                .retryLimit(2)
                .listener(customRetryListener)
                .build();
    }

    @Bean
    public ItemReader<Integer> retryListenerListItemReader() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        // LinkedListItemReader가 프로젝트 내 정의되어 있다면 유지하시고,
        // 표준 사용이라면 ListItemReader를 사용하세요.
        return new ListItemReader<>(list);
    }
}