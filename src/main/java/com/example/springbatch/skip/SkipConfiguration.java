package com.example.springbatch.skip;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class SkipConfiguration {

    @Bean
    public Job skipJob(JobRepository jobRepository, Step skipStep1) throws Exception {
        return new JobBuilder("skipJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(skipStep1)
                .build();
    }

    @Bean
    public Step skipStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("skipStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<String, String>chunk(5, transactionManager)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws SkippableException {
                        i++;
                        if(i == 3) {
                            throw new SkippableException("skip");
                        }
                        System.out.println("ItemReader : " + i);
                        return i > 20 ? null : String.valueOf(i);
                    }
                })
                .processor(skipProcessor())
                .writer(skipWriter())
                .faultTolerant()
//              .noSkip(SkippableException.class) // 아래 설정이 위의 설정을 덮어씀, skip() 설정이 우선
//              .skipPolicy(skipLimitCheckingItemSkipPolicy())
//              .retry(SkippableException.class)
//              .retryLimit(2)
                .skip(SkippableException.class)
                .skipLimit(2)
//              .noRollback(SkippableException.class)
                .build();
    }

    @Bean
    public LimitCheckingItemSkipPolicy skipLimitCheckingItemSkipPolicy(){

        Map<Class<? extends Throwable>, Boolean> skippableExceptionClasses = new HashMap<>();
        skippableExceptionClasses.put(SkippableException.class, true);

        return new LimitCheckingItemSkipPolicy(3, skippableExceptionClasses);
    }

    @Bean
    public SkipItemProcessor skipProcessor() {
        return new SkipItemProcessor();
    }

    @Bean
    public SkipItemWriter skipWriter() {
        return new SkipItemWriter();
    }
}