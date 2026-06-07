package com.example.springbatch.test.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import java.time.Duration;
import java.time.LocalDateTime;

public class JobListener implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {
        // Spring Batch 5에서는 LocalDateTime 타입을 반환합니다.
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        if (startTime != null && endTime != null) {
            // Duration을 사용하여 두 시간 사이의 차이를 구합니다.
            long durationMillis = Duration.between(startTime, endTime).toMillis();
            
            System.out.println("총 소요시간 : " + durationMillis + "ms");
        }
    }
}