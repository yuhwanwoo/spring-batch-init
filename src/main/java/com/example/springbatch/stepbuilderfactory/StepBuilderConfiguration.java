package com.example.springbatch.stepbuilderfactory;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.JobStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class StepBuilderConfiguration {

    // Job Step(step4) 실행을 위해 JobLauncher 필요
    private final JobLauncher jobLauncher;

    @Bean
    public Job stepBuilderBatchJob(JobRepository jobRepository,
                                   Step stepBuilderStep1,
                                   Step stepBuilderStep2,
                                   Step stepBuilderStep4,
                                   Step stepBuilderStep5) {
        return new JobBuilder("stepBuilderBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(stepBuilderStep1)
                .next(stepBuilderStep2)
                .next(stepBuilderStep4) // Job Step 실행
                .next(stepBuilderStep5) // Flow Step 실행
                .build();
    }

    // --- Steps ---

    @Bean
    public Step stepBuilderStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepBuilderStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step stepBuilderStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("stepBuilderStep2", jobRepository)
                .<String, String>chunk(3, transactionManager)
                .reader(itemReader()) // Reader 별도 메서드 혹은 람다
                .writer(itemWriter()) // Writer 별도 메서드 혹은 람다
                .build();
    }

    /*
    @Bean
    public Step stepBuilderStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        // Partition Step 예시 (주석 유지)
        return new StepBuilder("stepBuilderStep3", jobRepository)
                .partitioner("step1", new Partitioner() { ... })
                .gridSize(2)
                .build();
    }
    */

    @Bean
    public Step stepBuilderStep4(JobRepository jobRepository, Job stepBuilderSubJob) {
        // [중요] Job Step 설정: JobLauncher와 ParametersExtractor가 필요함
        return new StepBuilder("stepBuilderStep4", jobRepository)
                .job(stepBuilderSubJob)                 // 실행할 서브 Job
                .launcher(jobLauncher)                  // JobLauncher 필수
                .parametersExtractor(new DefaultJobParametersExtractor()) // 파라미터 전달 설정
                .build();
    }

    @Bean
    public Step stepBuilderStep5(JobRepository jobRepository, Flow stepBuilderFlow) {
        // Flow Step 설정
        return new StepBuilder("stepBuilderStep5", jobRepository)
                .flow(stepBuilderFlow)
                .build();
    }

    // --- Sub Job & Flow & Items ---

    @Bean
    public Job stepBuilderSubJob(JobRepository jobRepository, Step stepBuilderStep1, Step stepBuilderStep2) {
        return new JobBuilder("stepBuilderSubJob", jobRepository)
                .start(stepBuilderStep1)
                .next(stepBuilderStep2)
                .build();
    }

    @Bean
    public Flow stepBuilderFlow(Step stepBuilderStep2) {
        return new FlowBuilder<Flow>("stepBuilderFlow")
                .start(stepBuilderStep2)
                .end();
    }

    // (참고) Chunk Step용 Reader/Writer를 간단히 정의
    private ItemReader<String> itemReader() {
        return () -> null; // 더미 리더
    }

    private ItemWriter<String> itemWriter() {
        return list -> {}; // 더미 라이터
    }
}