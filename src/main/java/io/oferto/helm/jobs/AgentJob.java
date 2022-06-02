package io.oferto.helm.jobs;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.oferto.helm.tasklets.HelmTasklet;

@Configuration
@EnableBatchProcessing
public class AgentJob {
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private JobLauncher jobLauncher;

	@Scheduled(cron = "*/30 * * * * *")
	public void perform() throws Exception {
		System.out.println("Job Started at :" + new Date());

		JobParameters param = new JobParametersBuilder()
				.addString("JobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();

		JobExecution execution = jobLauncher.run(updateReleases(), param);

		System.out.println("Job finished with status :" + execution.getStatus());
	}
	
	@Bean
	public Job updateReleases() {
		return jobBuilderFactory.get("updateReleases")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
	}
	
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.tasklet(helmTasklet())
				.build();
	}
	
	@Bean
	public HelmTasklet helmTasklet() {
		HelmTasklet tasklet = new HelmTasklet();
		//tasklet.setDirectory(directory);
		
		return tasklet;
	}
	
	public JobLauncher getJobLauncher(JobRepository jobRepository) {
		SimpleJobLauncher launcher = new SimpleJobLauncher();
		launcher.setJobRepository(jobRepository);
		
		return launcher;
	}
}
