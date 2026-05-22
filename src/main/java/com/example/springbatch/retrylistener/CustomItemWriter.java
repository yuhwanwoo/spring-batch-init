package com.example.springbatch.retrylistener;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class CustomItemWriter implements ItemWriter<String> {

    private int count = 0;

    @Override
    public void write(Chunk<? extends String> chunk) throws CustomRetryException {
        // Spring Batch 5: Chunk 객체는 Iterable하므로 바로 루프를 돌릴 수 있습니다.
        for (String item : chunk) {
            if (count < 2) {
                if (count % 2 == 0) {
                    count = count + 1;
                } else if (count % 2 == 1) {
                    count = count + 1;
                    throw new CustomRetryException();
                }
            }
            System.out.println("write : " + item);
        }
    }
}