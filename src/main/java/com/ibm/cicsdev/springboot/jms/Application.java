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
		} catch(NamingException e) {
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
	public TaskExecutor taskExecutor()
	{
		return new DefaultManagedTaskExecutor();
	}

}
