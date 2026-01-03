package com.example.springbatch.joblauncher;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequiredArgsConstructor
public class JobLaunchingController {

    private final JobLauncher jobLauncher;

    // 이전 단계에서 만든 'launcherJob'을 주입받는다고 가정 (여러 Job이 있다면 Qualifier 필수)
    @Qualifier("launcherJob") 
    private final Job job;

    @PostMapping(value = "/batch")
    public String launch(@RequestBody Member member) throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("id", member.getId())
                .addDate("date", new Date())
                .toJobParameters();

        // JobLauncher가 설정(Config)에 의해 이미 비동기(Async)로 세팅되어 있다면
        // run 메서드는 즉시 리턴되고 배치는 백그라운드에서 돕니다.
        jobLauncher.run(job, jobParameters);

        System.out.println("Job is completed (Launch requested)");

        return "batch completed";
    }

    // (참고) 예제를 위한 Member DTO
    @Data
    public static class Member {
        private String id;
    }
}