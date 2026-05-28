package com.infybuzz.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.infybuzz.model.StudentCsv;
import com.infybuzz.writer.FirstItemWriter;

@Configuration
public class SampleJob {

	
	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private PlatformTransactionManager transactionManager;
	
//	@Autowired
//	private SecondTasklet secondTasklet;
	
//	@Autowired
//	private FirstJobListener firstJobListener;
	
//	@Autowired
//	private FirstStepListener firstStepListener;

//	@Autowired
//	private FirstItemReader firstItemReader;
	
//	@Autowired
//	private FirstItemProcessor firstItemProcessor;
	
	@Autowired
	private FirstItemWriter firstItemWriter;
	@Autowired 
	private static final Logger logger = LoggerFactory.getLogger(SampleJob.class);

	@Bean
	public Job firstJob() { 
		/*
		 * jobBuilderFactory.get("First Job")
		 * .start(firstStep())
		 * .next(secondStep())
		 */
		return new JobBuilder("First Job", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(firstStep())
//				.next(secondStep())
//				.listener(firstJobListener)
				.build();
	}
	public Step firstStep() {
		/*
		 * stepBuilderFactory.get("First Step")
		 * .tasklet()
  		 * .build()
		 */
		return new StepBuilder("First Step", jobRepository)
				.tasklet(firstTask(), transactionManager)
//				.listener(firstStepListener)
				.build();
	}
//	public Step secondStep() {
		/*
		 * stepBuilderFactory.get("Second Step")
		 * .tasklet()
		 * .build()
		 */
//		return new StepBuilder("Second Step", jobRepository)
//				.tasklet(secondTasklet, transactionManager)
//				.build();
//	}
	
	public Tasklet firstTask() { 
		
		  return new Tasklet() { 
		  	public RepeatStatus execute(StepContribution contribution, ChunkContext context) { 
		  		System.err.println("This is first tasklet step");
//		  		logger.warn("This is first tasklet step");
		  		System.err.println("SEC = " + context.getStepContext().getStepExecutionContext());
		  		return RepeatStatus.FINISHED;
		  	}
		  };
		 
//		return (contribution, context) -> { 
//			System.out.println("This is first tasklet step");
//			return RepeatStatus.FINISHED;
//		};
	}
	@Bean
	public Job secondJob() {
		return new JobBuilder("Second Job", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(firstChunkStep())
				.build();
	}
	
	public Step firstChunkStep() { 
		return new StepBuilder("First Chunk Step", jobRepository)
				.<StudentCsv, StudentCsv>chunk(3, transactionManager)
				.reader(flatFileItemReader())
//				.processor(firstItemProcessor)
				.writer(firstItemWriter)
				.build();
	}
	
	public FlatFileItemReader<StudentCsv> flatFileItemReader() { 
		FlatFileItemReader<StudentCsv> flatFileItemReader = 
				new FlatFileItemReader<StudentCsv>();
		flatFileItemReader.setResource(new ClassPathResource("InputFiles/students.csv"));
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
}
