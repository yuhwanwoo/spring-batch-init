package com.example.springbatch.jsonitemreader;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Customer {

	private long id;
	private String name;
	private int age;

}