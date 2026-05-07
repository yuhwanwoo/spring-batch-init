package com.example.springbatch.parallelsteps;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

public class StopWatchJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 시작 시점에 특별한 로직이 없다면 비워두셔도 무방합니다.
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        // Spring Batch 5부터는 LocalDateTime을 반환하므로 Duration을 사용해 계산합니다.
        if (startTime != null && endTime != null) {
            long time = Duration.between(startTime, endTime).toMillis();

            System.out.println("==========================================");
            System.out.println("총 소요된 시간 : " + time + "ms");
            System.out.println("==========================================");
        }
    }
}