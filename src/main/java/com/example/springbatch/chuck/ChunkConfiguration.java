package com.example.springbatch.chuck;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
public class ChunkConfiguration {

    @Bean
    public Job chunkConfigJob(JobRepository jobRepository, 
                              Step chunkConfigStep1, 
                              Step chunkConfigStep2) {
        return new JobBuilder("chunkConfigJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkConfigStep1)
                .next(chunkConfigStep2)
                .build();
    }

    @Bean
    public Step chunkConfigStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkConfigStep1", jobRepository)
                // Chunk 설정: (Chunk Size, TransactionManager)
                .<String, String>chunk(2, transactionManager)
                .reader(new ListItemReader<>(Arrays.asList("item1", "item2", "item3", "item4", "item5", "item6")))
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        Thread.sleep(300);
                        System.out.println("Processing: " + item);
                        return "my_" + item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    // Spring Batch 5: List -> Chunk 타입 변경
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        Thread.sleep(1000);
                        // Chunk 내부의 List를 꺼내서 출력
                        System.out.println("Writing: " + chunk.getItems());
                    }
                })
                .build();
    }

    @Bean
    public Step chunkConfigStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("chunkConfigStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}