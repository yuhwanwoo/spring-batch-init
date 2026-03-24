package com.example.springbatch.itemstream;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

public class CustomItemWriter implements ItemStreamWriter<String> {

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        // 기존 items.forEach 대신 chunk.getItems().forEach를 사용합니다.
        chunk.getItems().forEach(item -> System.out.println(item));
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("");
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("");
    }

    @Override
    public void close() throws ItemStreamException {
        System.out.println("");
    }
}