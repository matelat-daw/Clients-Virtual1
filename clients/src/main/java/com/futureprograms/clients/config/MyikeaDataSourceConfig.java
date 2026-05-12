package com.futureprograms.clients.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de la base de datos secundaria (myikea)
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.futureprograms.clients.repository.myikea",
    entityManagerFactoryRef = "myikeaEntityManagerFactory",
    transactionManagerRef = "myikeaTransactionManager"
)
public class MyikeaDataSourceConfig {

    @Value("${spring.datasource.myikea.url}")
    private String url;

    @Value("${spring.datasource.myikea.username}")
    private String username;

    @Value("${spring.datasource.myikea.password}")
    private String password;

    @Value("${spring.datasource.myikea.driver-class-name}")
    private String driverClassName;

    @Bean("myikeaDataSource")
    public DataSource myikeaDataSource() {
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

    @Bean("myikeaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean myikeaEntityManagerFactory(
            @Qualifier("myikeaDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.futureprograms.clients.entity.myikea");
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

    @Bean("myikeaTransactionManager")
    public PlatformTransactionManager myikeaTransactionManager(
            @Qualifier("myikeaEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
