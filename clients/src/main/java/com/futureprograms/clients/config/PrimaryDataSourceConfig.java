package com.futureprograms.clients.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de la base de datos primaria (clients)
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.futureprograms.clients.repository",
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.myikea\\..*"),
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Primary
    @Bean("primaryDataSource")
    public DataSource primaryDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
        
        // Hikari requiere jdbcUrl específicamente si no se mapea automáticamente
        if (dataSource.getJdbcUrl() == null) {
            dataSource.setJdbcUrl(url);
        }
        
        return dataSource;
    }

    @Primary
    @Bean("primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            @Qualifier("primaryDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.futureprograms.clients.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        // Configurar propiedades de Hibernate
        Map<String, Object> hibernateProperties = new HashMap<>();
        // Forzar el dialecto para evitar errores de detección con MySQL 8+
        hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        hibernateProperties.put("hibernate.ddl-auto", "update");
        hibernateProperties.put("hibernate.show_sql", false);
        hibernateProperties.put("hibernate.format_sql", true);
        
        // Evitar el error de la columna 'RESERVED' desactivando la consulta automática de metadatos
        hibernateProperties.put("hibernate.temp.use_jdbc_metadata_defaults", false);
        hibernateProperties.put("hibernate.jdbc.metadata_fetch_strategy", "MINIMAL");
        hibernateProperties.put("hibernate.dialect.storage_engine", "innodb");
        
        factory.setJpaPropertyMap(hibernateProperties);
        
        return factory;
    }

    @Primary
    @Bean("primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
