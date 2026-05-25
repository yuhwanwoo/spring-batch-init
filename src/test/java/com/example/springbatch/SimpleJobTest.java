package com.example.springbatch;

import com.example.springbatch.fortest.SimpleJobConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

@SpringBatchTest
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes={SimpleJobConfiguration.class, TestBatchConfig.class})
public class SimpleJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void simple_job_테스트() throws Exception {

        // given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("requestDate", "20020101")
                .addLong("date", new Date().getTime())
                .toJobParameters();

        // when
//        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step1");

        // then
        Assertions.assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();

        Assertions.assertEquals(stepExecution.getCommitCount(), 11);
        Assertions.assertEquals(stepExecution.getWriteCount(), 1000);
    }

    @AfterEach
    public void clear() throws Exception {
        jdbcTemplate.execute("delete from customer2");
    }
}