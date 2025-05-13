package com.mahitotsu.synerdesk;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {

        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
                DockerImageName.parse("public.ecr.aws/docker/library/postgres:15.13-alpine3.20")
                        .asCompatibleSubstituteFor("postgres"));
        container.withInitScripts("postgres/init/schema.sql");

        return container;
    }
}
