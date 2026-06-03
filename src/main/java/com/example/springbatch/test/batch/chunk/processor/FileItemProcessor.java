package com.example.springbatch.test.batch.chunk.processor;

import com.example.springbatch.test.batch.domain.Product;
import com.example.springbatch.test.batch.domain.ProductVO;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class FileItemProcessor implements ItemProcessor<ProductVO, Product> {

    @Override
    public Product process(ProductVO item) throws Exception {

        ModelMapper modelMapper = new ModelMapper();
        Product product = modelMapper.map(item, Product.class);


        return product;
    }
}