package com.example.springbatch.multithreadedstep;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

public class CustomWriteListener implements ItemWriteListener<Customer> {

    @Override
    public void beforeWrite(Chunk<? extends Customer> chunk) {
        // 필요한 로직을 구현하세요.
    }

    @Override
    public void afterWrite(Chunk<? extends Customer> chunk) {
        // Spring Batch 5: chunk.size()를 통해 처리된 아이템 개수를 가져옵니다.
        System.out.println("Thread : " + Thread.currentThread().getName() + ", write items : " + chunk.size());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Customer> chunk) {
        // 에러 발생 시 로직을 구현하세요.
    }
}