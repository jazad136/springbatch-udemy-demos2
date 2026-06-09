package com.infybuzz.config;

import javax.sql.DataSource;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class DatabaseConfig {
 
   @Bean
   @Primary
   @ConfigurationProperties(prefix = "spring.datasource")
   public DataSource datasource() {
      return DataSourceBuilder.create().build();
   }
   
   @Bean
   @ConfigurationProperties(prefix = "spring.universitydatasource")
   public DataSource universitydatasource() {
      return DataSourceBuilder.create().build();
   }
   
   @Bean
   @ConfigurationProperties(prefix = "spring.postgresdatasource")
   public DataSource postgresdatasource() {
      return DataSourceBuilder.create().build();
   }
 
   @Bean
   public EntityManagerFactory postgresqlEntityManagerFactory() {
      LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean =
            new LocalContainerEntityManagerFactoryBean();
      localContainerEntityManagerFactoryBean.setDataSource(postgresdatasource());
      localContainerEntityManagerFactoryBean.setPackagesToScan("com.infybuzz.postgresql.entity");
      localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
      localContainerEntityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
      localContainerEntityManagerFactoryBean.afterPropertiesSet();
 
      return localContainerEntityManagerFactoryBean.getObject();
   }
 
   @Bean
   public EntityManagerFactory mysqlEntityManagerFactory() {
      LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean =
            new LocalContainerEntityManagerFactoryBean();
      localContainerEntityManagerFactoryBean.setDataSource(universitydatasource());
      localContainerEntityManagerFactoryBean.setPackagesToScan("com.infybuzz.springbatch.mysql.entity");
      localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
      localContainerEntityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
      localContainerEntityManagerFactoryBean.afterPropertiesSet();
 
      return localContainerEntityManagerFactoryBean.getObject();
   }
 
   @Bean
   @Primary
   public JpaTransactionManager jpaTransactionManager() {
      JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
      jpaTransactionManager.setDataSource(universitydatasource());
      jpaTransactionManager.setEntityManagerFactory(mysqlEntityManagerFactory());
      return jpaTransactionManager;
   }
}
