package com.example.springbatch.itemreaderadapter;

public class CustomService<T> {

    public void customWrite(T item){

        System.out.println(item);
    }
}