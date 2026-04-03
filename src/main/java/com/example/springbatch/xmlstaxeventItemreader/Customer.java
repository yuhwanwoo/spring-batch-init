package com.example.springbatch.xmlstaxeventItemreader;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
public class Customer {

	private final long id;
	private final String name;
	private final int age;

}