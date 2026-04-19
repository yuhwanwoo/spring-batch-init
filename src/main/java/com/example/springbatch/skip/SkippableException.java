package com.example.springbatch.skip;

public class SkippableException extends Exception {

	public SkippableException() {
		super();
	}

	public SkippableException(String msg) {
		super(msg);
	}
}