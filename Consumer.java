package com.example.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;

import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    // If you choose to inject JobBuilderFactory/StepBuilderFactory provided by @EnableBatchProcessing,
    // Spring will create them for you. But if you want manual wiring, you can make them from
    // the JobRepository below.

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager txManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(txManager);

        // optional settings:
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        // factory.setTablePrefix("BATCH_"); // default prefix is BATCH_

        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.afterPropertiesSet();
        return launcher;
    }

    // Provided by EnableBatchProcessing: if you'd like, inject JobBuilderFactory/StepBuilderFactory
    @Bean
    public JobBuilderFactory jobBuilderFactory(JobRepository jobRepository) {
        return new JobBuilderFactory(jobRepository);
    }

    @Bean
    public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository,
                                                 PlatformTransactionManager txManager) {
        return new StepBuilderFactory(jobRepository, txManager);
    }

    // --- Example simple Tasklet Step and Job ---

    @Bean
    public Step exampleStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("exampleStep")
                .tasklet(exampleTasklet())
                .build();
    }

    @Bean
    public Tasklet exampleTasklet() {
        return (contribution, chunkContext) -> {
            System.out.println("Running example step. Current JVM default timezone: " + java.util.TimeZone.getDefault());
            // you can still inspect job times etc.
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Job exampleJob(JobBuilderFactory jobBuilderFactory, Step exampleStep, JobExecutionListener listener) {
        return jobBuilderFactory.get("exampleJob")
                .start(exampleStep)
                .listener(listener)
                .build();
    }

    // A simple listener for demonstration - optional.
    @Bean
    public JobExecutionListener listener() {
        return new org.springframework.batch.core.listener.JobExecutionListenerSupport() {
            @Override
            public void beforeJob(org.springframework.batch.core.JobExecution jobExecution) {
                System.out.println("Before job. JobExecution id: " + jobExecution.getId());
            }
        };
    }
}