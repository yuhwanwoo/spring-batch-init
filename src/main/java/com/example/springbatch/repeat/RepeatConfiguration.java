package com.example.springbatch.repeat;

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
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.exception.SimpleLimitExceptionHandler;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class RepeatConfiguration {

    @Bean
    public Job repeatJob(JobRepository jobRepository, Step repeatStep1) throws Exception {
        return new JobBuilder("repeatJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(repeatStep1)
                .build();
    }

    @Bean
    public Step repeatStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("repeatStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<String, String>chunk(5, transactionManager)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor(new ItemProcessor<String, String>() {

                    RepeatTemplate template = new RepeatTemplate();

                    @Override
                    public String process(String item) throws Exception {

                        // 반복할 때마다 count 변수의 값을 1씩 증가
                        // count 값이 chunkSize 값보다 크거나 같을 때 반복문 종료
//                        template.setCompletionPolicy(new SimpleCompletionPolicy(2));
                        // 소요된 시간이 설정된 시간보다 클 경우 반복문 종료
//                        template.setCompletionPolicy(new TimeoutTerminationPolicy(3000));

                        // 여러 유형의 CompletionPolicy 를 복합적으로 처리함
                        // 여러 개 중에 먼저 조건이 부합하는 CompletionPolicy 에 따라 반복문이 종료됨
//                        CompositeCompletionPolicy completionPolicy = new CompositeCompletionPolicy();
//                        CompletionPolicy[] completionPolicies = new CompletionPolicy[]{new TimeoutTerminationPolicy(3000),new SimpleCompletionPolicy(2)};
//                        completionPolicy.setPolicies(completionPolicies);
//                        template.setCompletionPolicy(completionPolicy);

                        // 예외 제한 횟수만큼 반복문 실행 (접두사가 적용된 Bean 호출)
                        template.setExceptionHandler(repeatSimpleLimitExceptionHandler());

                        template.iterate(new RepeatCallback() {
                            public RepeatStatus doInIteration(RepeatContext context) {
                                System.out.println("repeatTest");
                                throw new RuntimeException("Exception is occurred");
//                                return RepeatStatus.CONTINUABLE;
                            }
                        });

                        return item;
                    }
                })
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
    public SimpleLimitExceptionHandler repeatSimpleLimitExceptionHandler(){
        return new SimpleLimitExceptionHandler(2);
    }
}