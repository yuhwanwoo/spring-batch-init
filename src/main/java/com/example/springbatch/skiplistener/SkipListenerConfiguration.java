package com.example.springbatch.skiplistener;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class SkipListenerConfiguration {

    private final CustomSkipListener customSkipListener;

    @Bean
    public Job skipListenerJob(JobRepository jobRepository, Step skipListenerStep1) {
        return new JobBuilder("skipListenerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(skipListenerStep1)
                .build();
    }

    @Bean
    public Step skipListenerStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("skipListenerStep1", jobRepository)
                .<Integer, String>chunk(10, transactionManager)
                .reader(skipListenerListItemReader())
                .processor(item -> {
                    if (item == 4) {
                        throw new CustomSkipException("process skipped");
                    }
                    System.out.println("process : " + item);
                    return "item" + item;
                })
                .writer((Chunk<? extends String> chunk) -> {
                    // Spring Batch 5: ItemWriter는 Chunk 객체를 받습니다.
                    for (String item : chunk) {
                        if (item.equals("item5")) {
                            throw new CustomSkipException("write skipped");
                        }
                        System.out.println("write : " + item);
                    }
                })
                .faultTolerant()
                .skip(CustomSkipException.class)
                .skipLimit(3)
                .listener(customSkipListener)
                .build();
    }

    @Bean
    public ItemReader<Integer> skipListenerListItemReader() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // LinkedListItemReader가 프로젝트 내 정의되어 있다면 유지하시고,
        // 표준 사용이라면 ListItemReader를 사용하세요.
        return new ListItemReader<>(list);
    }
}