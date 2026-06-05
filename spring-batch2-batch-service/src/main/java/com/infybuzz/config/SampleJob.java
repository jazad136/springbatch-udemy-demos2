package com.infybuzz.config;

import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
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
import org.springframework.batch.item.xml.StaxEventItemWriter;
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

import com.infybuzz.listener.SkipListener;
import com.infybuzz.model.StudentCsv;
import com.infybuzz.model.StudentJdbc;
import com.infybuzz.model.StudentJson;
import com.infybuzz.model.StudentResponse;
import com.infybuzz.model.StudentXml;
import com.infybuzz.processor.FirstItemProcessor;
import com.infybuzz.service.StudentService;
import com.infybuzz.writer.FirstItemWriter;
@Configuration
public class SampleJob {

	
	private final FirstItemProcessor firstItemProcessor;
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

	@Autowired
	private SkipListener skipListener;
	
	@Autowired
	private SkipListener skipListenerImpl;
	
	SampleJob(FirstItemProcessor firstItemProcessor) {
		this.firstItemProcessor = firstItemProcessor;
	}

	@Bean
	public Job secondJob() {
		return new JobBuilder("Chunk Job", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(firstChunkStep())
				.build();
	}
	
	public Step firstChunkStep() { 
		return new StepBuilder("First Chunk Step", jobRepository)
				.<StudentCsv, StudentJson>chunk(3, transactionManager)
				.reader(flatFileItemReader(null))
				.processor(firstItemProcessor)
				.writer(jsonFileItemWriter(null))
				.faultTolerant()
				.skip(Throwable.class)
//				.skip(NullPointerException.class)
//				.skipLimit(Integer.MAX_VALUE)
//				.skipPolicy(new AlwaysSkipItemSkipPolicy())
				// do not skip unlimited and retry
				.skipLimit(100)
				.retryLimit(3)
				.retry(Throwable.class)
//				.listener(skipListener)
				.listener(skipListenerImpl)
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
		flatFileItemReader.setLinesToSkip(1);
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
				writer.write("Id, First Name, Last Name, Email");
				
			}
		});
		flatFileItemWriter.setLineAggregator(new DelimitedLineAggregator<StudentJdbc>(){
			{ 
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
						new JacksonJsonObjectMarshaller<StudentJson>()) {
				@Override
				public String doWrite(Chunk<? extends StudentJson> items) {
					items.getItems().stream().forEach(item -> {
						if(item.getId() == 3) { 
							System.err.println("Inside jsonFileItemWriter");
							throw new NullPointerException();
						}
					});
					return super.doWrite(items);
				}
		};
		
		return jsonFileItemWriter;
	}
	
	@StepScope
	@Bean
	public StaxEventItemWriter<StudentJdbc> staxEventItemWriter(
			@Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
		StaxEventItemWriter<StudentJdbc> staxEventItemWriter = 
				new StaxEventItemWriter<StudentJdbc>();
		staxEventItemWriter.setResource(fileSystemResource);
		staxEventItemWriter.setRootTagName("students");
		staxEventItemWriter.setMarshaller(new Jaxb2Marshaller() {
			{
				setClassesToBeBound(StudentJdbc.class);
			}
		});
		
		return staxEventItemWriter;
	}
	
	@Bean
	public JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter() {
		JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
		jdbcBatchItemWriter.setDataSource(universitydatasource);
		jdbcBatchItemWriter.setSql("insert into student(id, first_name, last_name, email)"
				+ "values (:id, :firstName, :lastName, :email)");
		jdbcBatchItemWriter.setItemSqlParameterSourceProvider(
				new BeanPropertyItemSqlParameterSourceProvider<StudentCsv>());
		
		return jdbcBatchItemWriter;
	}

	@Bean
	public JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter1() {
		JdbcBatchItemWriter<StudentCsv> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
		jdbcBatchItemWriter.setDataSource(universitydatasource);
		jdbcBatchItemWriter.setSql("insert into student(id, first_name, last_name, email)"
				+ "values (?, ?, ?, ?)");
		jdbcBatchItemWriter.setItemPreparedStatementSetter(new ItemPreparedStatementSetter<StudentCsv>() {
			@Override
			public void setValues(StudentCsv item, PreparedStatement ps) throws SQLException {
				ps.setLong(1, item.getId());
				ps.setString(2, item.getFirstName());
				ps.setString(3, item.getLastName());
				ps.setString(4, item.getEmail());
			}
		});
		return jdbcBatchItemWriter;
	}
	
	public ItemWriterAdapter<StudentCsv> itemWriterAdapter() { 
		ItemWriterAdapter<StudentCsv> itemWriterAdapter = new ItemWriterAdapter<StudentCsv>();

		itemWriterAdapter.setTargetObject(studentService);
		itemWriterAdapter.setTargetMethod("restCallToCreateStudent");
		// we don't need to worry about passing each StudentCsv into targetMethod
		// spring batch will handle it. 
		return itemWriterAdapter;
	}
}
