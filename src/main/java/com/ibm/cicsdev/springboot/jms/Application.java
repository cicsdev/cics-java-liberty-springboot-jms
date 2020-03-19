/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Copyright IBM Corp. 2019 All Rights Reserved   
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.cicsdev.springboot.jms;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.scheduling.concurrent.DefaultManagedTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * 
 * This class is the entry point of the spring boot application which contains @SpringBootApplication annotation and the main method to run the Spring Boot application.
 * 
 * A single @SpringBootApplication annotation can be used to enable those three features, that is:
 *
 *   @EnableAutoConfiguration: enable Spring Boot’s auto-configuration mechanism
 *   @ComponentScan: scan all the beans and package declarations when the application initializes.
 *   @Configuration: allow to register extra beans in the context or import additional configuration classes
 * 
 * @EnableJms: enable JMS listener annotated endpoints.
 * @EnableTransactionManagement: manage transaction
 */

@SpringBootApplication
@EnableJms
@EnableTransactionManagement
public class Application {

	private static final String CONNECTION_FACTORY = "jms/cf";

	public static void main(String[] args) {
		
		SpringApplication.run(Application.class, args);
		
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		
		try {
			// Look up the connection factory from Liberty
			ConnectionFactory fact = InitialContext.doLookup(CONNECTION_FACTORY);
			return fact;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Bean
	public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory) {
		
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setTaskExecutor(taskExecutor());
		factory.setTransactionManager(platformTransactionManager(connectionFactory));
		return factory;
	}

	@Bean
	public PlatformTransactionManager platformTransactionManager(ConnectionFactory connectionFactory) {

		try {
			UserTransaction tx = InitialContext.doLookup("java:comp/UserTransaction");
			return new JtaTransactionManager(tx);
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Bean
	public TaskExecutor taskExecutor() {
		
		return new DefaultManagedTaskExecutor();
	}

}
