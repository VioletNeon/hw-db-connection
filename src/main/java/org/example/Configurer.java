package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class Configurer {

    @Bean
    public HikariConfig dbConnectionPoolConfig() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://localhost:5433/pgsdb");
        config.setUsername("pgsuser");
        config.setPassword("pgspw");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("DemoHikariCP");
        config.setDriverClassName("org.postgresql.Driver");

        return config;
    }

    @Bean
    public DataSource dataSource(HikariConfig dbConnectionPoolConfig) {
        return new HikariDataSource(dbConnectionPoolConfig);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
