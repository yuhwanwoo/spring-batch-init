package com.example.springbatch.transition;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class TransitionConfiguration2 {

    @Bean
    public Job transitionBatchJob(JobRepository jobRepository,
                                  Step transitionStep1,
                                  Step transitionStep2, // Flow Step
                                  Step transitionStep5,
                                  Step transitionStep6) {
        return new JobBuilder("transitionBatchJob", jobRepository)
                .start(transitionStep1)
                .on("FAILED")
                .to(transitionStep2) // FAILED -> Step2(Flow) 실행
                .on("*")
                .stop()              // Step2 이후 그 외 상태는 STOP
                .from(transitionStep1)
                .on("*")
                .to(transitionStep5) // 그 외(성공 포함) -> Step5 실행
                .next(transitionStep6)
                .on("COMPLETED")
                .end()               // Step6 COMPLETED 시 종료
                .end()                   // Flow 구성 종료
                .build();
    }

    @Bean
    public Flow transitionFlow(Step transitionStep3, Step transitionStep4) {
        return new FlowBuilder<Flow>("transitionFlow")
                .start(transitionStep3)
                .next(transitionStep4)
                .end();
    }

    @Bean
    public Step transitionStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    // 테스트: 주석 해제 시 FAILED 상태 -> step2 실행 -> stop
                    // contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step transitionStep2(JobRepository jobRepository, Flow transitionFlow) {
        // Flow Step 설정
        return new StepBuilder("transitionStep2", jobRepository)
                .flow(transitionFlow)
                .build();
    }

    @Bean
    public Step transitionStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step transitionStep4(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step4 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step transitionStep5(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep5", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step5 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step transitionStep6(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("transitionStep6", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step6 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
