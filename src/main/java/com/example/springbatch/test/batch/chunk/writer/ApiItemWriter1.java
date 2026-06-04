package com.example.springbatch.test.batch.chunk.writer;


import com.example.springbatch.test.batch.domain.ApiRequestVO;
import com.example.springbatch.test.batch.domain.ApiResponseVO;
import com.example.springbatch.test.batch.service.AbstractApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

@Slf4j
public class ApiItemWriter1 extends FlatFileItemWriter<ApiRequestVO> {

    private final AbstractApiService apiService;

    public ApiItemWriter1(AbstractApiService apiService) {
        this.apiService = apiService;
        
        // 1. 초기 설정: 매번 open()을 호출하지 않도록 설정 정보를 미리 세팅
        setResource(new FileSystemResource("C:\\jsw\\inflearn\\spring-batch-lecture\\src\\main\\resources\\product1.txt"));
        setLineAggregator(new DelimitedLineAggregator<>());
        setAppendAllowed(true);
        // 2. 리소스 및 설정을 프레임워크가 인식하도록 호출
        try {
            afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize FlatFileItemWriter", e);
        }
    }

    @Override
    public void write(Chunk<? extends ApiRequestVO> chunk) throws Exception {
        System.out.println("----------------------------------");
        chunk.forEach(item -> System.out.println("items = " + item));
        System.out.println("----------------------------------");

        // API 서비스 호출
        ApiResponseVO response = apiService.service(chunk.getItems());
        System.out.println("response = " + response);

        // 결과 매핑
        chunk.forEach(item -> item.setApiResponseVO(response));

        // 3. 파일 쓰기 실행
        super.write(chunk);
    }
}