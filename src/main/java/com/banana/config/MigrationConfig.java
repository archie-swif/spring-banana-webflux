package com.banana.config;

import com.github.liquicouch.LiquiCouch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationConfig {

    @Autowired
    private ApplicationContext context;

    @Bean
    public LiquiCouch liquicouch() {
        LiquiCouch runner = new LiquiCouch(context);
        runner.setChangeLogsScanPackage("com.banana.migration");
        return runner;
    }
}
