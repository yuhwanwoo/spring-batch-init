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
public class ApiItemWriter3 extends FlatFileItemWriter<ApiRequestVO> {

    private final AbstractApiService apiService;

    public ApiItemWriter3(AbstractApiService apiService) {
        this.apiService = apiService;

        // 생성자에서 초기 설정을 완료하여 매 쓰기 시점마다 설정을 반복하지 않도록 합니다.
        setResource(new FileSystemResource("C:\\jsw\\inflearn\\spring-batch-lecture\\src\\main\\resources\\product3.txt"));
        setLineAggregator(new DelimitedLineAggregator<>());
        setAppendAllowed(true);
        
        try {
            afterPropertiesSet();
        } catch (Exception e) {
            log.error("Failed to initialize FlatFileItemWriter for product3.txt", e);
        }
    }

    @Override
    public void write(Chunk<? extends ApiRequestVO> chunk) throws Exception {
        System.out.println("----------------------------------");
        chunk.forEach(item -> System.out.println("items = " + item));
        System.out.println("----------------------------------");

        // API 서비스 호출: Chunk 객체에서 내부 리스트(getItems())를 추출하여 서비스에 전달
        ApiResponseVO response = apiService.service(chunk.getItems());
        System.out.println("response = " + response);

        // 결과 매핑: 각 아이템에 응답값 세팅
        chunk.forEach(item -> item.setApiResponseVO(response));

        // 파일 쓰기
        super.write(chunk);
    }
}