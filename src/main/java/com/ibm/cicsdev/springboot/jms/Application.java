/*                                                                        */
/* (c) Copyright IBM Corp. 2020 All Rights Reserved                       */
/*                                                                        */


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
 * @EnableTransactionManagement: manage transactions
 */
@SpringBootApplication
@EnableJms
@EnableTransactionManagement
public class Application 
{
    // JMS connection factory
    private static final String CONNECTION_FACTORY = "jms/cf1";
    
    public static void main(String[] args) 
    {    
        SpringApplication.run(Application.class, args);    
    }

    
    /**
     * Lookup the JMS connection factory and create the Spring bean
     * 
     * @return, the connection factory from Liberty
     */
    @Bean
    public ConnectionFactory connectionFactory() 
    {    
        try 
        {
            // Look up the connection factory from Liberty (server.xml) using JNDI
            ConnectionFactory factory = InitialContext.doLookup(CONNECTION_FACTORY);                    
            return factory;
        } 
        catch (NamingException e) 
        {
            e.printStackTrace();
            return null;
        }
    }
    

    /**
     * Create JMS listener for the MDP, setting the CF, task executor, and Txn Mgr
     * from Spring beans.
     * 
     * @param connectionFactory, the connection factory from Liberty
     * @return a JMS listener container from the factory for the MDP
     */
    @Bean
    public JmsListenerContainerFactory<?> myFactoryBean(ConnectionFactory connectionFactory) 
    {    
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setTaskExecutor(taskExecutor());
        factory.setTransactionManager(platformTransactionManager(connectionFactory));
        return factory;
    }

    
    /**
     *  Create a JTA enabled Spring TransactionManager.
     * 
     * @param connectionFactory, the connection factory from Liberty
     * @return a JtaTransactionManager to manage the transaction
     */
    @Bean
    public PlatformTransactionManager platformTransactionManager(ConnectionFactory connectionFactory) 
    {
        try 
        {
            // Use JNDI to lookup Liberty's transaction context, and return a transaction manager from it
            UserTransaction tx = InitialContext.doLookup("java:comp/UserTransaction");
            return new JtaTransactionManager(tx);
        } 
        catch (NamingException e) 
        {
            e.printStackTrace();
            return null;
        }
    }

    
    /**
     * Create Liberty executor, so threads are CICS enabled
     * 
     * @return the DefaultManagedTaskExecutor
     */
    @Bean
    public TaskExecutor taskExecutor() 
    {    
        return new DefaultManagedTaskExecutor();
    }

}
