package com.example.springbatch.skip;

public class NoSkipException extends Exception {

	public NoSkipException() {
		super();
	}

	public NoSkipException(String msg) {
		super(msg);
	}
}