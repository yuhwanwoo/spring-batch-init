package com.example.springbatch.itemstream;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class ItemStreamConfiguration {

    @Bean
    public Job itemStreamJob(JobRepository jobRepository, 
                             Step itemStreamStep1, 
                             Step itemStreamStep2) {
        return new JobBuilder("itemStreamJob", jobRepository)
                .start(itemStreamStep1)
                .next(itemStreamStep2)
                .build();
    }

    @Bean
    public Step itemStreamStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("itemStreamStep1", jobRepository)
                // Spring Batch 5: chunk 선언 시 트랜잭션 매니저 필수
                .<String, String>chunk(5, transactionManager)
                .reader(itemStreamReader())
                .writer(itemStreamWriter())
                .build();
    }

    @Bean
    public CustomItemStreamReader itemStreamReader() {
        List<String> items = new ArrayList<>(10);

        for(int i = 1; i <= 10; i++) {
            items.add(String.valueOf(i));
        }

        return new CustomItemStreamReader(items);
    }

    @Bean
    public ItemWriter<String> itemStreamWriter() {
        // 제네릭 타입 <String> 명시
        return new CustomItemWriter();
    }

    @Bean
    public Step itemStreamStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("itemStreamStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}