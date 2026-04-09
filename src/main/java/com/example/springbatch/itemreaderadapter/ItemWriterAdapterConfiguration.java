package com.example.springbatch.itemreaderadapter;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ItemWriterAdapterConfiguration {

    @Bean
    public Job itemWriterAdapterJob(JobRepository jobRepository, Step itemWriterAdapterStep1) throws Exception {
        return new JobBuilder("itemWriterAdapterJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(itemWriterAdapterStep1)
                .build();
    }

    @Bean
    public Step itemWriterAdapterStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("itemWriterAdapterStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<String, String>chunk(10, transactionManager)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 10 ? null : "item" + i;
                    }
                })
                .writer(itemWriterAdapterCustomItemWriter())
                .build();
    }

    @Bean
    public ItemWriterAdapter<String> itemWriterAdapterCustomItemWriter() {
        // 제네릭 <String>을 명시하여 타입 경고 제거
        ItemWriterAdapter<String> writer = new ItemWriterAdapter<>();
        writer.setTargetObject(itemWriterAdapterCustomService());
        
        // CustomService 클래스에 선언된 joinCustomer 메서드가 호출됩니다.
        writer.setTargetMethod("joinCustomer");
        return writer;
    }

    @Bean
    public CustomService itemWriterAdapterCustomService() {
        return new CustomService();
    }
}