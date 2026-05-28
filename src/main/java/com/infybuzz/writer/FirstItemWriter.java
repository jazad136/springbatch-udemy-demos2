package com.infybuzz.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.infybuzz.model.StudentCsv;

@Component
public class FirstItemWriter implements ItemWriter<StudentCsv> {
	@Override
	public void write(Chunk<? extends StudentCsv> items) throws Exception {
		System.err.println("Inside Item Writer");
		items.forEach(System.err::println);
	}
}
