package com.example.springbatch.test.batch.scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FileSchJob extends QuartzJobBean {

    @Autowired
    private Job fileJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobExplorer jobExplorer;

    @SneakyThrows
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        String requestDate = (String) context.getJobDetail().getJobDataMap().get("requestDate");

        // 1. Spring Batch 5 방식의 JobParameters 생성
        // Date.getTime() 대신 현재 시간을 밀리초 단위의 Long으로 처리
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("id", LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .addString("requestDate", requestDate)
                .toJobParameters();

        // 2. JobInstance 확인 로직
        List<JobInstance> jobInstances = jobExplorer.getJobInstances(fileJob.getName(), 0, Integer.MAX_VALUE);

        for (JobInstance jobInstance : jobInstances) {
            List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
            
            // 3. 파라미터 비교 (Spring Batch 5의 JobParameters API 활용)
            boolean exists = jobExecutions.stream()
                    .anyMatch(execution -> 
                        requestDate.equals(execution.getJobParameters().getString("requestDate")));

            if (exists) {
                log.warn("{} already exists", requestDate);
                throw new JobExecutionException(requestDate + " already exists");
            }
        }

        // 4. Job 실행
        jobLauncher.run(fileJob, jobParameters);
    }
}