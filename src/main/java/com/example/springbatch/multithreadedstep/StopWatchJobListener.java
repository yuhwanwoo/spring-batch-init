package com.example.springbatch.multithreadedstep;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;
import java.time.LocalDateTime;

public class StopWatchJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // 시작 전 특별한 로직이 없다면 비워둡니다.
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();

        // Spring Batch 5: getStartTime/EndTime이 LocalDateTime을 반환하므로 
        // Duration 클래스를 사용하여 밀리초(ms) 단위 차이를 계산합니다.
        if (startTime != null && endTime != null) {
            long time = Duration.between(startTime, endTime).toMillis();

            System.out.println("==========================================");
            System.out.println("총 소요된 시간 : " + time + "ms");
            System.out.println("==========================================");
        }
    }
}