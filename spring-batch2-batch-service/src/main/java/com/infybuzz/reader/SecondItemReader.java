package com.infybuzz.reader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

@Component
public class SecondItemReader implements ItemReader<Integer>{
	List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
	Stream<Integer> stream;	
	boolean called;
	public SecondItemReader() { 
		stream = list.stream();
	}
	@Override
	public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if(stream.iterator().hasNext()) { 
			Integer out = stream.iterator().next();
			return out;
		}
		else { 
			stream = list.stream();
			return null;
		}
	}
}
