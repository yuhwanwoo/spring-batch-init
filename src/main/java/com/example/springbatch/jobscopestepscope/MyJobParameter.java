package com.example.springbatch.jobscopestepscope;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class MyJobParameter implements Tasklet {
    @Value("#{jobParameters[name]}")
    private String name;

    @Nullable
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        return RepeatStatus.FINISHED;
    }

    public void requestDate(String requestDate) {
        System.out.println("name = " + name);
        System.out.println("requestDate = " + requestDate);
    }
}
