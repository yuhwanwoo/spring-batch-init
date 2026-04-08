package com.example.springbatch.jdbcpagingitemreader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

// Spring Boot 3.x 이상에서는 javax 대신 jakarta를 사용합니다.
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class JpaConfiguration {

    // Spring Batch 5: Factory 대신 DataSource와 EntityManagerFactory만 주입받습니다.
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job jpaConfigurationJob(JobRepository jobRepository, Step jpaConfigurationStep1) throws Exception {
        return new JobBuilder("jpaConfigurationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(jpaConfigurationStep1)
                .build();
    }

    @Bean
    public Step jpaConfigurationStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("jpaConfigurationStep1", jobRepository)
                // Spring Batch 5: 트랜잭션 매니저 필수 추가
                .<Customer, Customer2>chunk(10, transactionManager)
                .reader(jpaConfigurationItemReader())
                .processor(jpaConfigurationItemProcess())
                .writer(jpaConfigurationItemWriter())
                .build();
    }

    @Bean
    public JpaItemWriter<Customer2> jpaConfigurationItemWriter() {
        return new JpaItemWriterBuilder<Customer2>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

    @Bean
    public ItemProcessor<Customer, Customer2> jpaConfigurationItemProcess() {
        // 기존의 와일드카드(? super, ? extends)를 명시적 제네릭으로 깔끔하게 처리했습니다.
        return new CustomItemProcess();
    }

    @Bean
    public JdbcPagingItemReader<Customer> jpaConfigurationItemReader() {

        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(10);
        reader.setRowMapper(new CustomerRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, firstName, lastName, birthdate");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where firstname like :firstname");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);
        reader.setQueryProvider(queryProvider);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "C%");

        reader.setParameterValues(parameters);

        return reader;
    }
}