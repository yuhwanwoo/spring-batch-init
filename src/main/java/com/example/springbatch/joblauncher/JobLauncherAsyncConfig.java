package com.example.springbatch.joblauncher;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class JobLauncherAsyncConfig {

    /**
     * 기본 JobLauncher를 오버라이딩하여 비동기(Async) 실행을 가능하게 합니다.
     * 이렇게 설정하면 컨트롤러에서 jobLauncher.run() 호출 시 기다리지 않고 즉시 반환됩니다.
     */
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        
        // 여기서 비동기 Executor를 설정 (기존 코드의 의도 반영)
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor()); 
        
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}