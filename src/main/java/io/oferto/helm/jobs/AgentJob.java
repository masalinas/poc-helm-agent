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
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${HELM_COMMAND:#{null}}")
	String helmCommand;

	@Value("${HELM_REPO:#{null}}")
	String helmRepo;

	@Scheduled(cron = "${JOB_CRON_CONFIG:0 0/1 * * * *}")
	public void jobScheduler() throws Exception {
		System.out.println("Job Started at :" + new Date());

		JobParameters jobParameters = new JobParametersBuilder()
				.addString("JobID", String.valueOf(System.currentTimeMillis()))
				.addString("helmCommand", helmCommand)
				.addString("helmRepo", helmRepo)
				.toJobParameters();

		JobExecution jobExecution = jobLauncher.run(updateReleases(), jobParameters);

		System.out.println("Job finished with status :" + jobExecution.getStatus());
	}

	@Bean
	public Job updateReleases() {
		return jobBuilderFactory.get("updateReleases")
				.incrementer(new RunIdIncrementer())
				.flow(step())
				.end()
				.build();
	}

	@Bean
	public Step step() {
		return stepBuilderFactory.get("step")
				.tasklet(helmTasklet())
				.build();
	}

	@Bean
	public HelmTasklet helmTasklet() {
		HelmTasklet tasklet = new HelmTasklet();

		return tasklet;
	}

	public JobLauncher getJobLauncher(JobRepository jobRepository) {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);

		return jobLauncher;
	}
}
