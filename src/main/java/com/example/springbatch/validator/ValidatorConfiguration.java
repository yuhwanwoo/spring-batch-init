package com.example.springbatch.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ValidatorConfiguration {

    @Bean
    public Job validatorJob(JobRepository jobRepository, 
                            Step validatorStep1, 
                            Step validatorStep2, 
                            Step validatorStep3) {
        return new JobBuilder("validatorJob", jobRepository)
                /* * Validator 설정:
                 * 커스텀 Validator 혹은 DefaultJobParametersValidator 사용 가능
                 */
                .validator(new CustomJobParametersValidator())
//              .validator(new DefaultJobParametersValidator(new String[]{"name"}, new String[]{"year"}))
                .start(validatorStep1)
                .next(validatorStep2)
                .next(validatorStep3)
                .build();
    }

    @Bean
    public Step validatorStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("validatorStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step validatorStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("validatorStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step2 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step validatorStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("validatorStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("step3 has executed");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}