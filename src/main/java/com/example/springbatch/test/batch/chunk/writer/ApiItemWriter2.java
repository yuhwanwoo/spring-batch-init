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
public class ApiItemWriter2 extends FlatFileItemWriter<ApiRequestVO> {

    private final AbstractApiService apiService;

    public ApiItemWriter2(AbstractApiService apiService) {
        this.apiService = apiService;

        // 초기 설정: 매 호출마다 open()을 할 필요가 없도록 생성자에서 설정
        setResource(new FileSystemResource("C:\\jsw\\inflearn\\spring-batch-lecture\\src\\main\\resources\\product2.txt"));
        setLineAggregator(new DelimitedLineAggregator<>());
        setAppendAllowed(true);
        
        try {
            // 필수 속성 체크 및 초기화
            afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize FlatFileItemWriter for product2.txt", e);
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

        // 파일 쓰기
        super.write(chunk);
    }
}