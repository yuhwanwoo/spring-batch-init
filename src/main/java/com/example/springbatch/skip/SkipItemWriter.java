package com.example.springbatch.skip;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class SkipItemWriter implements ItemWriter<String> {

    private int cnt = 0;

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        // 기존 items 대신 chunk.getItems()를 사용하여 순회합니다.
        for (String item : chunk.getItems()) {
            if(item.equals("-12")) {
                System.out.println("ItemWriter : " + item);
                cnt++;
                throw new SkippableException("Write failed. cnt:" + cnt);
            }
            else {
                System.out.println("ItemWriter : " + item);
            }
        }
    }
}