package com.infybuzz.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FirstItemWriter implements ItemWriter<Long> {
	@Override
	public void write(Chunk<? extends Long> items) throws Exception {
		System.err.println("Inside Item Writer");
		items.forEach(System.err::println);
	}
}
