@Bean
public JobRepository jobRepository(DataSource dataSource) throws Exception {
    JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
    factory.setDataSource(dataSource);
    factory.setTransactionManager(transactionManager());
    factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
    factory.setTablePrefix("BATCH_");

    // important part:
    factory.setJdbcOperations(new JdbcTemplate(dataSource) {{
        setQueryTimeout(10000);
    }});

    // Force UTC
    TimeZone utc = TimeZone.getTimeZone("UTC");
    factory.setTimeZone(utc);

    return factory.getObject();
}