package com.example.springbatch.test.batch.chunk.processor;

import com.example.springbatch.test.batch.domain.ApiRequestVO;
import com.example.springbatch.test.batch.domain.ProductVO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ApiItemProcessor3 implements ItemProcessor<ProductVO, ApiRequestVO> {

    @Override
    public ApiRequestVO process(ProductVO productVO) throws Exception {

        return ApiRequestVO.builder()
                .id(productVO.getId())
                .productVO(productVO)
                .build();
    }
}