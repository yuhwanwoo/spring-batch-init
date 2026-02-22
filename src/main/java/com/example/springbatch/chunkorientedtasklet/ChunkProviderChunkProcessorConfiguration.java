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
public class ChunkProviderChunkProcessorConfiguration {

    @Bean
    public Job chunkProviderChunkProcessorJob(JobRepository jobRepository, 
                                              Step chunkProviderChunkProcessorStep1, 
                                              Step chunkProviderChunkProcessorStep2) {
        return new JobBuilder("chunkProviderChunkProcessorJob", jobRepository)
                .start(chunkProviderChunkProcessorStep1)
                .next(chunkProviderChunkProcessorStep2)
                .build();
    }

    @Bean
    @JobScope
    public Step chunkProviderChunkProcessorStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkProviderChunkProcessorStep1", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저를 파라미터로 전달
                .<String, String>chunk(2, transactionManager)
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5", "item6")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        return "my_" + item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    // Spring Batch 5: List<? extends String> items -> Chunk<? extends String> chunk
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        chunk.getItems().forEach(item -> System.out.println(item));
                    }
                })
                .build();
    }

    @Bean
    public Step chunkProviderChunkProcessorStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkProviderChunkProcessorStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}