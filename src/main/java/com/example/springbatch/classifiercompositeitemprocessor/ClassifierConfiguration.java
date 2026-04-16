package com.example.springbatch.classifiercompositeitemprocessor;

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
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ClassifierConfiguration {

    @Bean
    public Job classifierJob(JobRepository jobRepository, Step classifierStep1) throws Exception {
        return new JobBuilder("classifierJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(classifierStep1)
                .build();
    }

    @Bean
    public Step classifierStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("classifierStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<ProcessorInfo, ProcessorInfo>chunk(10, transactionManager)
                .reader(new ItemReader<ProcessorInfo>() {
                    int i = 0;
                    @Override
                    public ProcessorInfo read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        ProcessorInfo processorInfo = ProcessorInfo.builder().id(i).build();
                        return i > 3 ? null : processorInfo;
                    }
                })
                .processor(classifierItemProcessor())
                .writer(new ItemWriter<ProcessorInfo>() {
                    @Override
                    // Spring Batch 5: List -> Chunk 타입 변경
                    public void write(Chunk<? extends ProcessorInfo> chunk) throws Exception {
                        System.out.println(chunk.getItems());
                    }
                })
                .build();
    }

    @Bean
    public ItemProcessor<ProcessorInfo, ProcessorInfo> classifierItemProcessor() {

        ClassifierCompositeItemProcessor<ProcessorInfo, ProcessorInfo> processor = new ClassifierCompositeItemProcessor<>();

        // ProcessorClassifier는 기존에 직접 작성하신 커스텀 클래스로 가정하고 구조를 유지했습니다.
        ProcessorClassifier<ProcessorInfo, ItemProcessor<?, ? extends ProcessorInfo>> classifier = new ProcessorClassifier<>();
        
        Map<Integer, ItemProcessor<ProcessorInfo, ProcessorInfo>> processorMap = new HashMap<>();
        processorMap.put(1, new CustomItemProcessor1());
        processorMap.put(2, new CustomItemProcessor2());
        processorMap.put(3, new CustomItemProcessor3());
        
        classifier.setProcessorMap(processorMap);
        processor.setClassifier(classifier);

        return processor;
    }
}