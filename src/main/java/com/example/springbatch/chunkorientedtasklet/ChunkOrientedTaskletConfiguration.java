package com.example.springbatch.chunkorientedtasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
public class ChunkOrientedTaskletConfiguration {

    @Bean
    public Job chunkOrientedTaskletJob(JobRepository jobRepository,
                                       Step chunkOrientedTaskletStep1,
                                       Step chunkOrientedTaskletStep2) {
        return new JobBuilder("chunkOrientedTaskletJob", jobRepository)
                .start(chunkOrientedTaskletStep1)
                .next(chunkOrientedTaskletStep2)
                .build();
    }

    @Bean
    @JobScope
    public Step chunkOrientedTaskletStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkOrientedTaskletStep1", jobRepository)
                // Spring Batch 5: chunk 설정에 TransactionManager 필수
                .<String, String>chunk(3, transactionManager)
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5", "item6")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        return "my_" + item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    // Spring Batch 5: List -> Chunk 타입으로 변경
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        // Chunk 내부의 아이템들을 순회하며 출력
                        chunk.getItems().forEach(item -> System.out.println(item));
                    }
                })
                .build();
    }

    @Bean
    public Step chunkOrientedTaskletStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkOrientedTaskletStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}