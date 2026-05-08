package com.objectservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.objectservice.repository")
public class R2dbcConfig {
    // Connection pool is auto-configured by Spring Boot via spring.r2dbc.pool.* in application.yml
}
