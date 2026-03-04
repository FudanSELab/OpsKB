package com.xidian.kg.config;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class Neo4jOgmConfig {

    @Value("${spring.data.neo4j.uri:bolt://localhost:7687}")
    private String uri;

    @Value("${spring.data.neo4j.username:neo4j}")
    private String username;

    @Value("${spring.data.neo4j.password:neo4j}")
    private String password;

    @Bean(name = "ogmConfiguration")
    public org.neo4j.ogm.config.Configuration ogmConfiguration() {
        return new org.neo4j.ogm.config.Configuration.Builder()
                .uri(uri)
                .credentials(username, password)
                .build();
    }

    @Bean(name = "sessionFactory")
    public SessionFactory sessionFactory(org.neo4j.ogm.config.Configuration configuration) {
        return new SessionFactory(configuration, "com.xidian.kg");
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new Neo4jTransactionManager(sessionFactory);
    }
}

