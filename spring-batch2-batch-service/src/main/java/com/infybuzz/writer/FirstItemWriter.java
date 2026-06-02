package com.infybuzz.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.infybuzz.model.StudentResponse;

@Component
public class FirstItemWriter implements ItemWriter<StudentResponse> {
	@Override
	public void write(Chunk<? extends StudentResponse> items) throws Exception {
		System.err.println("Inside Item Writer");
		items.forEach(System.err::println);
	}
}
