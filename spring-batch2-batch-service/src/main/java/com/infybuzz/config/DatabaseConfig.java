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

	public EntityManagerFactory postgresqlEntityManagerFactory(DataSource postgresdatasource) {
		LocalContainerEntityManagerFactoryBean lem = new LocalContainerEntityManagerFactoryBean();
		lem.setDataSource(postgresdatasource);
		lem.setPackagesToScan("com.infybuzz.postgresql.entity");
		lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
		lem.afterPropertiesSet();
		return lem.getObject();
	}

	public EntityManagerFactory mysqlEntityManagerFactory(DataSource universitydatasource) {
		LocalContainerEntityManagerFactoryBean lem = new LocalContainerEntityManagerFactoryBean();
		lem.setDataSource(universitydatasource);
		lem.setPackagesToScan("com.infybuzz.mysql.entity");
		lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
		lem.afterPropertiesSet();
		return lem.getObject();
	}


	@Bean
	@Primary
	public JpaTransactionManager jpaTransactionManager(DataSource universitydatasource, EntityManagerFactory mysqlEntityManagerFactory) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setDataSource(universitydatasource);
		jpaTransactionManager.setEntityManagerFactory(mysqlEntityManagerFactory);
		return jpaTransactionManager;
	}
}
