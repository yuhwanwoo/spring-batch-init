package com.example.springbatch.retry.template;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class RetryItemWriter2 implements ItemWriter<Customer> {

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        // Spring Batch 5: List 대신 Chunk 객체에서 getItems()를 호출하여 순회합니다.
        chunk.getItems().forEach(item -> System.out.println(item));
    }
}