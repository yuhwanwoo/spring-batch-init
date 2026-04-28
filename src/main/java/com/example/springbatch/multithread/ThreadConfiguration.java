package com.example.springbatch.multithread;

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
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class ThreadConfiguration {

    @Bean
    public Job threadJob(JobRepository jobRepository, Step threadStep1) {
        return new JobBuilder("threadJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(threadStep1)
                .build();
    }

    @Bean
    public Step threadStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("threadStep1", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저가 필수로 포함됩니다.
                .<String, String>chunk(5, transactionManager)
                .reader(threadSynchronizedReader())
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) {
                        System.out.println("Processor => Thread = " + Thread.currentThread().getName() + " | item = " + item);
                        return item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    // Spring Batch 5: List 대신 Chunk 객체를 사용합니다.
                    public void write(Chunk<? extends String> chunk) {
                        System.out.println("Writer => Thread = " + Thread.currentThread().getName() + " | items = " + chunk.getItems());
                    }
                })
                // 멀티 스레드 환경 설정을 위한 TaskExecutor 추가
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .throttleLimit(4)
                .build();
    }

    @Bean
    public SynchronizedItemStreamReader<String> threadSynchronizedReader() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add(String.valueOf(i));
        }

        // CustomListItemReader가 ListItemReader를 상속받거나 구현한 것으로 가정합니다.
        CustomListItemReader<String> customListItemReader = new CustomListItemReader<>(items);

        // 빌더 패턴을 사용하여 SynchronizedItemStreamReader를 생성하는 것이 권장됩니다.
        return new SynchronizedItemStreamReaderBuilder<String>()
                .delegate(customListItemReader)
                .build();
    }

    @Bean
    public ListItemReader<String> threadReader2() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add(String.valueOf(i));
        }
        return new CustomListItemReader<>(items);
    }
}