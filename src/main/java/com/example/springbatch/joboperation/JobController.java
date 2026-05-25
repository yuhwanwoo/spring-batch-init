package com.example.springbatch.joboperation;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;
import java.util.Set;

@RestController
public class JobController {

    private final JobRegistry jobRegistry;
    private final JobOperator jobOperator;
    private final JobExplorer jobExplorer;
    // 변환을 위한 컨버터 추가
    private final JobParametersConverter jobParametersConverter = new DefaultJobParametersConverter();

    public JobController(JobRegistry jobRegistry, JobOperator jobOperator, JobExplorer jobExplorer) {
        this.jobRegistry = jobRegistry;
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
    }

    @PostMapping(value = "/batch/start")
    public String start(@RequestBody JobInfo jobInfo) throws Exception {
        for (String jobName : jobRegistry.getJobNames()) {
            System.out.println("job name: " + jobName);

            // 1. JobParameters 생성
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("id", jobInfo.getId())
                    .toJobParameters();

            // 2. DefaultJobParametersConverter를 사용하여 Properties로 변환
            Properties properties = jobParametersConverter.getProperties(jobParameters);
            
            // 3. JobOperator 실행
            jobOperator.start(jobName, properties);
        }
        return "batch is started";
    }

    @PostMapping(value = "/batch/restart")
    public String restart() throws Exception {
        for (String jobName : jobRegistry.getJobNames()) {
            JobInstance lastJobInstance = jobExplorer.getLastJobInstance(jobName);
            if (lastJobInstance != null) {
                JobExecution lastJobExecution = jobExplorer.getLastJobExecution(lastJobInstance);
                if (lastJobExecution != null) {
                    jobOperator.restart(lastJobExecution.getId());
                }
            }
        }
        return "batch is restarted";
    }

    @PostMapping(value = "/batch/stop")
    public String stop() throws Exception {
        for (String jobName : jobRegistry.getJobNames()) {
            Set<JobExecution> runningJobExecutions = jobExplorer.findRunningJobExecutions(jobName);
            for (JobExecution jobExecution : runningJobExecutions) {
                jobOperator.stop(jobExecution.getId());
            }
        }
        return "batch is stopped";
    }
}