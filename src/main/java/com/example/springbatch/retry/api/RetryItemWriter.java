package com.example.springbatch.retry.api;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class RetryItemWriter implements ItemWriter<Customer> {

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        // Spring Batch 5: List 대신 Chunk 객체에서 getItems()를 호출하여 순회합니다.
        chunk.getItems().forEach(item -> System.out.println(item));
    }
}