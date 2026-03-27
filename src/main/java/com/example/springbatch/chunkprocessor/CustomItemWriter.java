package com.example.springbatch.chunkprocessor;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class CustomItemWriter implements ItemWriter<Customer> {

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        chunk.getItems().forEach(item -> System.out.println(item));
    }
}