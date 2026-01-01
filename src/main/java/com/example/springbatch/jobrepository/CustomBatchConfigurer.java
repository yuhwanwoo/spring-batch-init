package com.example.springbatch.jobrepository;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class CustomBatchConfigurer {
    // Spring Batch 5 (Boot 3) 방식:
    // Configurer를 상속받지 않고, JobRepository Bean을 직접 정의하여 덮어씌웁니다.

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();

        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);

        // 1. Isolation Level 설정
        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");

        // 2. Table Prefix 설정 (기본값 "BATCH_")
        // 만약 테이블 명을 SYSTEM_JOB_EXECUTION 등으로 쓰고 싶다면 "SYSTEM_" 으로 변경
        factory.setTablePrefix("BATCH_");

        // 참고: setMaxVarCharLength(1000) 메서드는 Spring Batch 5.0에서 삭제되었습니다.
        // Batch 5부터는 ExecutionContext 직렬화 방식이 변경되어 해당 설정이 더 이상 유효하지 않거나
        // 다른 방식(Serializer 커스텀)으로 처리해야 합니다.

        // FactoryBean 초기화 (필수)
        factory.afterPropertiesSet();

        return factory.getObject();
    }
}