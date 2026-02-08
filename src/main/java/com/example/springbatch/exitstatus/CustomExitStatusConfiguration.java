package com.example.springbatch.exitstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class CustomExitStatusConfiguration {

    @Bean
    public Job customExitStatusBatchJob(JobRepository jobRepository, 
                                        Step customExitStatusStep1, 
                                        Step customExitStatusStep2) {
        return new JobBuilder("customExitStatusBatchJob", jobRepository)
                .start(customExitStatusStep1)
                    .on("FAILED")
                    .to(customExitStatusStep2) // Step1이 FAILED면 Step2 실행
                    .on("PASS")                // Step2의 결과가 PASS면
                    .stop()                    // 멈춤
                .end()
                .build();
    }

    @Bean
    public Step customExitStatusStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("customExitStatusStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step1 has executed");
                    // Step1을 강제로 FAILED 상태로 종료 -> Step2로 넘어감
                    contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step customExitStatusStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("customExitStatusStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println(">> step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(new PassCheckingListener()) // 커스텀 리스너 등록
                .build();
    }

    /**
     * Spring Batch 5: StepExecutionListenerSupport가 Deprecated 되었으므로
     * StepExecutionListener 인터페이스를 직접 구현합니다.
     */
    static class PassCheckingListener implements StepExecutionListener {

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            String exitCode = stepExecution.getExitStatus().getExitCode();
            
            // FAILED가 아니라면 ExitStatus를 "PASS"로 변경하여 리턴
            if (!exitCode.equals(ExitStatus.FAILED.getExitCode())) {
                return new ExitStatus("PASS"); // Job 흐름의 .on("PASS")와 매칭됨
            } else {
                return null; // null을 반환하면 기존 ExitStatus 유지
            }
        }
    }
}