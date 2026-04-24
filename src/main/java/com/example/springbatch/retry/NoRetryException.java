package com.example.springbatch.retry;

public class NoRetryException extends RuntimeException {

	public NoRetryException() {
		super();
	}
	public NoRetryException(String msg) {
		super(msg);
	}
}