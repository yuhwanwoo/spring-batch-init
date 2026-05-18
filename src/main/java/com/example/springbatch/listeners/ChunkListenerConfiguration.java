package com.example.springbatch.listeners;

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
public class ChunkListenerConfiguration {

    private final CustomChunkListener customChunkListener;

    @Bean
    public Job chunkListenerJob(JobRepository jobRepository, Step chunkListenerStep1) throws Exception {
        return new JobBuilder("chunkListenerJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkListenerStep1)
                .build();
    }

    @Bean
    public Step chunkListenerStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("chunkListenerStep1", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저를 필수로 함께 넘겨줍니다.
                .<Integer, String>chunk(10, transactionManager)
                .listener(customChunkListener)
                .listener(new CustomItemReadListener())
                .listener(new CustomItemProcessListener())
                .listener(new CustomItemWriteListener<String>()) // 이전 턴에서 제네릭으로 수정한 규격 반영
                .reader(chunkListenerListItemReader())
                .processor(item -> {
                    // 테스트를 위한 예외 발생 코드
                    throw new RuntimeException("failed");
                    // return "item" + item;
                })
                .writer(chunk -> {
                    // Spring Batch 5: ItemWriter는 List가 아닌 Chunk<? extends T>를 받습니다.
                    throw new RuntimeException("failed");
                })
                .build();
    }

    @Bean
    public ItemReader<Integer> chunkListenerListItemReader() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        return new ListItemReader<>(list);
    }
}