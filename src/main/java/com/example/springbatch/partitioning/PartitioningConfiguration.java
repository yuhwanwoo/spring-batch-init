package com.example.springbatch.partitioning;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class PartitioningConfiguration {

    private final DataSource dataSource;

    @Bean
    public Job partitionJob(JobRepository jobRepository, Step partitionMasterStep) {
        return new JobBuilder("partitionJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(partitionMasterStep)
                .build();
    }

    @Bean
    public Step partitionMasterStep(JobRepository jobRepository, Step partitionSlaveStep) {
        return new StepBuilder("masterStep", jobRepository)
                // Slave Step과 Partitioner를 연결
                .partitioner(partitionSlaveStep.getName(), partitionColumnRangePartitioner())
                .step(partitionSlaveStep)
                .gridSize(4) // 파티션 개수
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step partitionSlaveStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("slaveStep", jobRepository)
                // Spring Batch 5: chunk 설정 시 트랜잭션 매니저 필수
                .<Customer, Customer>chunk(1000, transactionManager)
                .reader(partitionPagingItemReader(null, null))
                .writer(partitionCustomerItemWriter())
                .build();
    }

    @Bean
    public ColumnRangePartitioner partitionColumnRangePartitioner() {
        ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
        columnRangePartitioner.setColumn("id");
        columnRangePartitioner.setDataSource(this.dataSource);
        columnRangePartitioner.setTable("customer");
        return columnRangePartitioner;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Customer> partitionPagingItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {
        
        System.out.println("reading " + minValue + " to " + maxValue);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
        // 파티셔닝된 범위를 Where 절에 바인딩
        queryProvider.setWhereClause("where id >= " + minValue + " and id < " + maxValue);

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("partitionPagingItemReader")
                .dataSource(this.dataSource)
                .fetchSize(1000)
                .rowMapper(new CustomerRowMapper())
                .queryProvider(queryProvider)
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> partitionCustomerItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(this.dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}