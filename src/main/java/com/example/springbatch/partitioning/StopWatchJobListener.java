package com.example.springbatch.partitioning;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

public class StopWatchJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 시작 시점에 특별한 로직이 필요 없다면 비워둡니다.
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        // Spring Batch 5: getStartTime/EndTime이 LocalDateTime을 반환합니다.
        if (startTime != null && endTime != null) {
            // Duration을 사용하여 두 시간 사이의 차이를 계산합니다.
            long time = Duration.between(startTime, endTime).toMillis();

            System.out.println("==========================================");
            System.out.println("작업 종료 상태 : " + jobExecution.getStatus());
            System.out.println("총 소요된 시간 : " + time + "ms");
            System.out.println("==========================================");
        }
    }
}