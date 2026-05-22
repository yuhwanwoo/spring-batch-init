package com.example.springbatch.retrylistener;

public class CustomRetryException extends Exception {

    public CustomRetryException() {
        super();
    }

    public CustomRetryException(String message) {
        super(message);
    }
}