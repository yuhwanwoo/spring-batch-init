package com.example.springbatch.listeners;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Component
public class CustomItemWriteListener<T> implements ItemWriteListener<T> {

    @Override
    public void beforeWrite(Chunk<? extends T> chunk) {
        System.out.println(">> beforeWrite");
    }

    @Override
    public void afterWrite(Chunk<? extends T> chunk) {
        // Spring Batch 5: chunk.getItems()를 통해 내부 리스트 데이터를 참조합니다.
        System.out.println(">> afterWrite : " + chunk.getItems());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends T> chunk) {
        System.out.println(">> onWriteError : " + exception.getMessage());
        System.out.println(">> onWriteError : " + chunk.getItems());
    }
}