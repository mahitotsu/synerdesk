package com.mahitotsu.synerdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.mahitotsu.synerdesk.definition.AgentDefinitionLookup;
import com.mahitotsu.synerdesk.definition.BeanFactoryAgentDefinitionLookup;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

@SpringBootApplication
public class Main {

    public static void main(final String... args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeAsyncClient() {
        return BedrockAgentRuntimeAsyncClient.create();
    }

    @Bean
    public AgentDefinitionLookup agentDefinitionLookup() {
        return new BeanFactoryAgentDefinitionLookup();
    }
}
