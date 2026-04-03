package com.example.springbatch.xmlstaxeventItemreader;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class XMLConfiguration {

    @Bean
    public Job xmlConfigurationJob(JobRepository jobRepository, Step xmlConfigurationStep1) {
        return new JobBuilder("xmlConfigurationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(xmlConfigurationStep1)
                .build();
    }

    @Bean
    public Step xmlConfigurationStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("xmlConfigurationStep1", jobRepository)
                .<Customer, Customer>chunk(3, transactionManager)
                .reader(xmlConfigurationItemReader())
                .writer(xmlConfigurationItemWriter())
                .build();
    }

    @Bean
    public StaxEventItemReader<Customer> xmlConfigurationItemReader() {
        return new StaxEventItemReaderBuilder<Customer>()
                .name("xmlFileItemReader")
                .resource(new ClassPathResource("customer.xml"))
                .addFragmentRootElements("customer")
                // XStream 대신 Jaxb2Marshaller 사용
                .unmarshaller(xmlConfigurationItemMarshaller()) 
                .build();
    }

    @Bean
    public ItemWriter<Customer> xmlConfigurationItemWriter() {
        return chunk -> {
            for (Customer item : chunk.getItems()) {
                System.out.println(item.toString());
            }
        };
    }

    // 완전히 변경된 Marshaller 설정 (Jaxb2Marshaller)
    @Bean
    public Jaxb2Marshaller xmlConfigurationItemMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        // 변환할 클래스 타입을 명시해 줍니다.
        jaxb2Marshaller.setClassesToBeBound(Customer.class);
        return jaxb2Marshaller;
    }
}