package com.infybuzz.config;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import com.infybuzz.model.StudentCsv;
import com.infybuzz.model.StudentJdbc;
import com.infybuzz.model.StudentJson;
import com.infybuzz.model.StudentResponse;
import com.infybuzz.model.StudentXml;
import com.infybuzz.service.StudentService;
import com.infybuzz.writer.FirstItemWriter;
@Configuration
public class SampleJob {

	
	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private FirstItemWriter firstItemWriter;
	
	@Autowired 
	private static final Logger logger = LoggerFactory.getLogger(SampleJob.class);

	@Autowired
	@Qualifier("datasource")
	private DataSource datasource;
	
	@Autowired
	@Qualifier("universitydatasource")
	private DataSource universitydatasource;
	
	@Autowired
	private StudentService studentService;

	@Bean
	public Job secondJob() {
		return new JobBuilder("Second Job", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(firstChunkStep())
				.build();
	}
	
	public Step firstChunkStep() { 
		return new StepBuilder("First Chunk Step", jobRepository)
				.<StudentJdbc, StudentJson>chunk(3, transactionManager)
//				.reader(flatFileItemReader(null))
//				.reader(jsonItemReader(null))
//				.reader(staxEventItemReader(null))
				.reader(jdbcCursorItemReader())
//				.reader(itemReaderAdapter())
//				.processor(firstItemProcessor)
//				.writer(firstItemWriter)
//				.writer(flatFileItemWriter(null))
				.writer(jsonFileItemWriter(null))
				.build();
	}
	
	@StepScope
	@Bean
	public FlatFileItemReader<StudentCsv> flatFileItemReader(
			@Value("#{jobParameters['inputFile']}") ClassPathResource classPathResource
	) { 
		FlatFileItemReader<StudentCsv> flatFileItemReader = 
				new FlatFileItemReader<StudentCsv>();
//		flatFileItemReader.setResource(new ClassPathResource("InputFiles/students.csv"));
		flatFileItemReader.setResource(classPathResource);

		flatFileItemReader.setLineMapper(new DefaultLineMapper<StudentCsv>() { 
			{
				setLineTokenizer(new DelimitedLineTokenizer() { 
					{
						setNames("ID", "First Name", "Last Name", "Email");
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<StudentCsv>() {
					{
						setTargetType(StudentCsv.class);
					}
				});
			}
		});
		/* For reference
		DefaultLineMapper<StudentCsv> defaultLineMapper = new DefaultLineMapper<StudentCsv>();
		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		delimitedLineTokenizer.setNames("ID", "First Name", "Last Name", "Email");

		defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);

		BeanWrapperFieldSetMapper<StudentCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(StudentCsv.class);
		defaultLineMapper.setFieldSetMapper(fieldSetMapper);
		flatFileItemReader.setLineMapper(defaultLineMapper);
		flatFileItemReader.setLinesToSkip(1);
		*/
		return flatFileItemReader;
		
	}
	
	@StepScope
	@Bean
	public JsonItemReader<StudentJson> jsonItemReader(
			@Value("#{jobParameters['inputFile']}") ClassPathResource classPathResource) 
	{ 	
		JsonItemReader<StudentJson> jsonItemReader = new JsonItemReader<StudentJson>();
		jsonItemReader.setResource(classPathResource);
		jsonItemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(StudentJson.class));
		
		return jsonItemReader;
	}
	
	@StepScope
	@Bean
	public StaxEventItemReader<StudentXml> staxEventItemReader(
			@Value("#{jobParameters['inputFile']}") ClassPathResource classPathResource) 
	{
		StaxEventItemReader<StudentXml> staxEventItemReader = 
				new StaxEventItemReader<StudentXml>();
		staxEventItemReader.setResource(classPathResource);
		staxEventItemReader.setFragmentRootElementName("student");
		staxEventItemReader.setUnmarshaller(new Jaxb2Marshaller() {
			{
				setClassesToBeBound(StudentXml.class);
			}
		});
		return staxEventItemReader;
	}
	
	public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader() 
	{ 
		var jdbcCursorItemReader = new JdbcCursorItemReader<StudentJdbc>();
		jdbcCursorItemReader.setDataSource(universitydatasource);
		jdbcCursorItemReader.setSql(
			"select id "
			+ ",first_name as firstName "
			+ ",last_name as lastName "
			+ ",email"
			+ " from student");
		jdbcCursorItemReader.setRowMapper(new BeanPropertyRowMapper<StudentJdbc>() {
			{
				setMappedClass(StudentJdbc.class);
			}
		});
		return jdbcCursorItemReader;
	}
	public ItemReaderAdapter<StudentResponse> itemReaderAdapter() {
		ItemReaderAdapter<StudentResponse> itemReaderAdapter = new ItemReaderAdapter<StudentResponse>();

		itemReaderAdapter.setTargetObject(studentService);
		itemReaderAdapter.setTargetMethod("getStudent");
		itemReaderAdapter.setArguments(new Object[] {1L, "Test"});

		return itemReaderAdapter;
	}
	
	@StepScope
	@Bean
	public FlatFileItemWriter<StudentJdbc> flatFileItemWriter(
			@Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) { 
		FlatFileItemWriter<StudentJdbc> flatFileItemWriter =
				new FlatFileItemWriter<StudentJdbc>();
		flatFileItemWriter.setResource(fileSystemResource);
		flatFileItemWriter.setHeaderCallback(new FlatFileHeaderCallback() 
		{ 	
			@Override
			public void writeHeader(Writer writer) throws IOException {
				writer.write("Id|First Name|Last Name|Email");
				
			}
		});
		flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<StudentJdbc>(){
			{ 
				setDelimiter("|");
				setFieldExtractor(new BeanWrapperFieldExtractor<StudentJdbc>() {
					{
						setNames(new String[] {"id", "firstName", "lastName", "email"});
					}
				});
			}
		});
		flatFileItemWriter.setFooterCallback(new FlatFileFooterCallback() {
			public void writeFooter(Writer writer) throws IOException { 
				writer.write("Created @ " + new Date());
			}
		});
		return flatFileItemWriter;
	}
	@StepScope
	@Bean
	public JsonFileItemWriter<StudentJson> jsonFileItemWriter(
			@Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
		JsonFileItemWriter<StudentJson> jsonFileItemWriter = 
				new JsonFileItemWriter<>(fileSystemResource, 
						new JacksonJsonObjectMarshaller<StudentJson>());
		return jsonFileItemWriter;
	}
}
