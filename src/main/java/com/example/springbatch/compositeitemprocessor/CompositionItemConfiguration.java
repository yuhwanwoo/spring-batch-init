package com.example.springbatch.compositeitemprocessor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class CompositionItemConfiguration {

    @Bean
    public Job compositionItemJob(JobRepository jobRepository, Step compositionItemStep1) throws Exception {
        return new JobBuilder("compositionItemJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(compositionItemStep1)
                .build();
    }

    @Bean
    public Step compositionItemStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("compositionItemStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<String, String>chunk(10, transactionManager)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 10 ? null : "item";
                    }
                })
                .processor(compositionItemProcessor())
                .writer(new ItemWriter<String>() {
                    @Override
                    // Spring Batch 5: List -> Chunk 타입 변경
                    public void write(Chunk<? extends String> chunk) throws Exception {
                        System.out.println(chunk.getItems());
                    }
                })
                .build();
    }

    @Bean
    public CompositeItemProcessor<String, String> compositionItemProcessor() {
        
        // 제네릭을 명시하여 타입 안정성 확보 및 경고 제거
        List<ItemProcessor<String, String>> itemProcessors = new ArrayList<>();
        itemProcessors.add(new CustomItemProcessor1());
        itemProcessors.add(new CustomItemProcessor2());

        return new CompositeItemProcessorBuilder<String, String>()
                .delegates(itemProcessors)
                .build();
    }
}