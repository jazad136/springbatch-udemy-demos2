package com.infybuzz.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.infybuzz.model.StudentJdbc;

@Component
public class FirstItemWriter implements ItemWriter<StudentJdbc> {
	@Override
	public void write(Chunk<? extends StudentJdbc> items) throws Exception {
		System.err.println("Inside Item Writer");
		items.forEach(System.err::println);
	}
}
