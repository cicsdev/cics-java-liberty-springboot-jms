# Spring Boot Java applications for CICS, Part 5: JMS

** THIS IS NOT YET READY FOR REVIEW **

## Introduction 

Spring Boot provides a range of support for integrating with messaging systems, from simplified use of the JMS API using JmsTemplate to a *message driven POJO* (MDP) to handle 
incoming asynchronous message using the `@JmsListener` annotation. 
This tutorial, the fifth in the [Spring Boot Java applications for CICS](https://developer.ibm.com/series/learning-path-spring-boot-java-applications-for-cics/) series, 
demonstrates how to set up a Spring Boot JMS application integrated with CICS, build it with Maven or Gradle, 
and finally deploy and test it in a CICS Liberty JVM server using IBM MQ series as the JMS provider. 


## Learning Objectives

This tutorial will show you how to:
1. Create and build a Spring Boot application that uses JMS
1. Send a simple JMS message using Spring's `JmsTemplate`
1. Receive a JMS message using an message driven POJO (MDP)
1. Understand how to add transaction management to the MDP
1. Test the sample in CICS

The sample is a web application and all requests can be made from a browser. The application uses the Spring Boot web interface to process GET REST requests. 
In a real-world implementation, other types of REST interfaces, such as POST, would be more appropriate. GET requests are used here for simplicity.

The application source and build scripts are available in the [cicsdev/cics-java-liberty-springboot-jms](https://github.com/cicsdev/cics-java-liberty-springboot-jms) repository.

## Prerequisites

1. CICS TS V5.3 or later
1. A configured Liberty JVM server in CICS
1. IBM MQ V8.0 or later on z/OS
1. Java SE 1.8 on the z/OS system
1. Java SE 1.8 on the workstation
1. An Eclipse development environment on the workstation (optional)
1. Either Gradle or Apache Maven on the workstation (optional if using Wrappers)


## Estimated time

It should take you about 2 hours to complete this tutorial.

## Steps

### 1 Create and build a Spring Boot application 


You can develop the code by following this tutorial step-by-step, or by downloading the [cics-java-liberty-springboot-jms](https://github.com/cicsdev/cics-java-liberty-springboot-jms) example in GitHub.

If you are following along step-by-step, generate and download a Spring Boot web application using the Spring initializr website tool. 
For further details on how to do this, refer to part 1 of this tutorial series, [Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven](https://developer.ibm.com/technologies/java/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle).
Eclipse is used as the preferred IDE.

Once your newly generated project has been imported into your IDE, you should have the `Application.java` and `ServletInitializer.java` classes which provide the basic framework of a Spring Boot web application.

For Gradle, your build file will need the following additional the dependencies `spring-integration-jms`  `javax.transaction-api` and `javax.jms-api` over and above the components required for the Part 1 tutorial.
The Java EE JMS and JTA dependencies are marked as `complileOnly` as the Liberty server Java EE runtime provides its own implementations, and so they are not required to be packaged in the build.
Your gradle.build file should look like this.

``` gradle    
dependencies 
{
    implementation("org.springframework.boot:spring-boot-starter-web")

    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
  
    compileOnly enforcedPlatform("com.ibm.cics:com.ibm.cics.ts.bom:5.5-20200519131930-PH25409")
      
    compileOnly("com.ibm.cics:com.ibm.cics.server")          
    
    implementation("org.springframework.integration:spring-integration-jms")
	
    compileOnly("javax.jms:javax.jms-api")  
    
    compileOnly("javax.transaction:javax.transaction-api")     
}   
```

For Maven, the equivalent pom.xml dependencies should look like this:

``` maven    
    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>com.ibm.cics</groupId>
            <artifactId>com.ibm.cics.server</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.integration</groupId>
            <artifactId>spring-integration-jms</artifactId>
        </dependency>

        <dependency>
            <groupId>javax.transaction</groupId>
            <artifactId>javax.transaction-api</artifactId>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>javax.jms</groupId>
            <artifactId>javax.jms-api</artifactId>
            <scope>provided</scope>
        </dependency>

    </dependencies>
  

```

### 2 Send a simple JMS message


In this section, you’ll learn how to send a simple JMS message to an MQ queue using Spring’s `JmsTemplate` and a JMS connection factory.

The first job is to update our Spring Application class to create a Spring Bean which returns the JMS connection factory from the Liberty server configuration. 
We will use the JNDI name `jms/cf` for our connection factory as follows:
``` java

	private static final String CONNECTION_FACTORY = "jms/cf";	

	@Bean
	public ConnectionFactory connectionFactory() 
	{	
		try 
		{			
			ConnectionFactory factory = InitialContext.doLookup(CONNECTION_FACTORY);					
			return factory;
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
````

Next we need to add a REST controller class called `JMSMessageSendController`, this will provide a REST API to invoke the JMS send operation. 

``` java
@RestController
public class JMSMessageSendController 
{
    @Autowired
    private JmsTemplate jmsTemplate;
     
    @GetMapping("/")
    public String root() 
    {                        
        Date myDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss.SSSSSS");
        String myDateString = sdf.format(myDate);
        
        return "<h1>Spring Boot JMS REST sample usage: Date/Time: " + myDateString + "</h1>"
        + "<h3>Usage:</h3>"
        + "<b>/send/{queue}?data={input string}</b> - write input string to specified queue <br>"
        ;
    }        
    
    
    @RequestMapping("/send/{jmsq}")
    public String send(@RequestParam(value = "data") String inputStr, @PathVariable String jmsq) 
    {
       
        try {
            jmsTemplate.convertAndSend(jmsq, inputStr);
        }
        catch (JmsException jre) {
            return "JMSMessageSendController - ERROR on JMS send " + jre.getMessage();   

        }

        return inputStr;
    }
	
}		
```


The `JmsTemplate` interface is the central class in Spring’s JMS core package. It simplifies JMS operations leaving application code to provide the input and extract results.
We inject the JmsTemplate using the `@Autowired` annotation.

The `root()` method defines a mapping to the root URL `/` using the `@GetMapping("/")` annotation and returns the usage for the sample. 

The `send()` method is annotated with the `@RequestMapping("/send/{jmsq}")` and takes as input the name of the JMS queue as a URI path parameter, 
and an input query string to be written to the queue.
This means the URL format for our endpoint will be `/send/{queue}?data={input string}` where `queue` is the name of the MQ queue. 
We use the `convertAndSend()` method on the `JmsTemplate` to write the input string to the JMS queue. 
This method converts the input String to a JMS message. You can modify the conversion behaviour if required by 
implementing the `MessageConverter` interface and providing your own implementation of the methods `fromMessage()` and `toMessage()`.


### 3 Recieve a JMS message using an MDP

Although JmsTemplate can be used to send and receive messages, it can't easily be used to initiate work from incoming messages.
Instead Spring provides an asynchronous message driven POJO (MDP) to provide a message based application entry point, similar to Java EE message driven beans.

We will create a new class called *JMSMessageReceiver* to act as our MDP.  

``` java
@Component
public class JMSMessageReceiver 
{    
    private static final String MDP_QUEUE = "SPRING.QUEUE";
    private static final String TSQ_NAME = "SPRINGQ";
    
    @JmsListener(destination = MDP_QUEUE, containerFactory = "myFactoryBean")
    public void receiveMessage(String data) throws Exception 
    {    
        System.out.println("Received <" + data + ">");

        TSQ tsq = new TSQ();
        tsq.setName(TSQ_NAME);
        tsq.writeString(data);
      
    }
}
```
JMSMessageReceiver is annotated with `@Component` so that it is found during classpath scanning and registered as a Spring Bean. 
This bean uses the `receiveMessage()` method to receive the input string from the JMS queue. 

The `@JmsListener` annotation on the `receiveMessage()` method defines the name of the destination queue
that this method should listen to and the reference to the `JmsListenerContainerFactory` to use to create the underlying message listener. 
The JMS listener containerFactory `myFactoryBean` must be created as a Spring Bean in the Application class and takes as input the connection factory. 
Add the following code to the Application class to define `myFactoryBean` .

``` java
	@Bean
	public JmsListenerContainerFactory<?> myFactoryBean(ConnectionFactory connectionFactory) 
	{	
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setTaskExecutor(taskExecutor());		
		return factory;
	}
```


Note that the container factory requires that the task executor is updated using `setTaskExecutor(taskExecutor())`. This is to permit incoming requests to the MDP
to be able to use the JCICS API as we use the `TSQ` JCICS class to write the input string to the CICS temporary storage queue called "SPRINGQ".

Without these changes requests to the JmsListenerContainerFactory won't run on a CICS-enabled thread and if a request tries to use the JCICS API, you will get the error below:

``` text
org.springframework.jms.listener.adapter.ListenerExecutionFailedException: Listener method 'public void com.ibm.cicsdev.springboot.jms.smokeTest.JMSMessageReceiver.receiveMessage(java.lang.String) throws com.ibm.cics.server.CicsException' threw exception; nested exception is com.ibm.cics.server.CicsRuntimeException: DTCTSQ\_WRITE: No JCICS context is associated with the current thread.
```

The task executor we need to use is based on the `DefaultManagedTaskExecutor` provided by Liberty.  
This provides integration for CICS-enabled threads as it looks up the application server's defaultExecutor, which in CICS Liberty is designed to provide CICS enabled threads.
To create this task executor add the following `taskExecutor()` method to the Application class.

``` java
	@Bean
	public TaskExecutor taskExecutor() 
	{	
		return new DefaultManagedTaskExecutor();
	}
 ```

The last step is to add the `concurrent-1.0` Liberty feature to the Liberty feature manager list in server.xml, to support the usage of the task executor. 
Having done this the MDP should be able to receive messages written to the SPRING.QUEUE and then write to the SPRINGQ TSQ using the JCICS API.


### 4 Add transaction management

In order to make our JMSMessageReceiver class fully transactional we need to make sure that the reading of the message from the JMS queue is coordinated with the writing to the CICS TSQ.
JMS sends and receives do not operate under the control of the CICS unit-of-work when using the JMS resource adapter in client mode, and so in order to coordinate these two actions 
we need to use Spring Transactions.
`PlatformTransactionManager` is the central interface in Spring's transaction infrastructure and  
Spring makes available several implementations of the platform transaction manager interface for handling transactions across JDBC, JPA, Hibernate, JMS and so on. 

For JMS, you can use either the Spring `JmsTransactionManager` which implements a local transaction using the `PlatformTransactionManager`, or Spring's `JtaTransactionManager` which provides global transaction
support based on JTA.
In order to integrate recoverable operations across our JMSListner and CICS TSQ resource we will need to use the Spring `JtaTransactionManager`, which is further described in
[Spring Boot Java applications for CICS, Part 3: Transactions](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-3-transactions)

Based on the above knowledge, we need to first amend our Spring Application class by adding a new Spring Bean for a JTA enabled PlatformTransactionManager as follows.

``` java
	@Bean
	public PlatformTransactionManager platformTransactionManager(ConnectionFactory connectionFactory) 
	{
		try 
		{	
			UserTransaction tx = InitialContext.doLookup("java:comp/UserTransaction");
			return new JtaTransactionManager(tx);
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
```

Next we need to update the `myFactoryBean` in the Application class to set the transaction manager for our JMS listener as follows:
``` java
	@Bean
	public JmsListenerContainerFactory<?> myFactoryBean(ConnectionFactory connectionFactory) 
	{	
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setTaskExecutor(taskExecutor());		
		factory.setTransactionManager(platformTransactionManager(connectionFactory));
		return factory;
	}
```

Lastly we need to add the `@Transactional (rollbackFor=Exception.class)` annotation to the `receiveMessage()` method in our `JMSMessageReceiver` class 
to denote this as a Spring Boot container managed transaction.
The`rollbackFor=Exception.class` will ensure that the method rolls back for any exceptions, not just unchecked Exceptions.

Next we will add a test on the input message string `data`, and if the data equals "rollback" then we will throw an Exception causing a rollback of the JTA transaction.
Equally if the JCICS `TSQ.writeString()` method fails with a checked `CICSConditionExcption`, this will likewise cause a rollback.


``` java
@Component
public class JMSMessageReceiver 
{    
    private static final String MDP_QUEUE = "SPRING.QUEUE";
    private static final String TSQ_NAME = "SPRINGQ";
        
    @Transactional (rollbackFor=Exception.class)
    @JmsListener(destination = MDP_QUEUE, containerFactory = "myFactoryBean")
    public void receiveMessage(String data) throws Exception 
    {    
        System.out.println("Received <" + data + ">");
        
        TSQ tsq = new TSQ();
        tsq.setName(TSQ_NAME);
        tsq.writeString(data);        
        
        if (data.equalsIgnoreCase("rollback"))
        {   
            System.out.println("Rolling back");
            throw new Exception("Expected rollback exception");
        } 
        
        else 
        {            
            System.out.println("Committing");
        }
    }
}
```
	
After the above configuration, our MDP will be able to receive messages and update the CICS TSQ under a single global transaction, 
meaning both operations always commit or both rollback if an Exception is thrown.


> Note: If application security is enabled in the target Liberty server, you need to enable an authentication method and authorisation roles. 
To do this, create a Java EE `web.xml` file and place it in the `src/main/webapp/WEB-INF` folder. 
A sample web.xml file that supports basic authentication is provided in the associated Git repository. 
For further details on enabling security, refer to the previous tutorial, [Spring Boot Java applications for CICS, Part 2: Security](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-2-security/).


### 5  Deploy and run the sample

To deploy the sample into a CICS Liberty JVM server, you need to build the application as a WAR. 
Gradle `build.gradle` and Maven `pom.xml` files are provided in the sample repository to simplify this task. 
Once built, there are a couple of ways to deploy the application:

- Add an `<application>` element to the Liberty server.xml that points directly to the WAR.
- Add the WAR to a CICS bundle project, exporting the project to zFS, and install it using a CICS BUNDLE resource definition

In addition you will need to:
- Create an MQ queue called SPRING.QUEUE
- Add a CICS TSMODEL definiton for the temporary storage queue SPRINGQ, defined as recoverable.
- Add the MQ resource adapter and a connection factory definition in Liberty server.xml.


Add the following Liberty features to your server.xml

-   `servlet-3.1` or `servlet-4.0`
-   `wmqJmsClient-2.0`
-   `concurrent-1.0`

> For further details on configuring Liberty and deploying the sample to CICS, see the [README](https://github.com/cicsdev/cics-java-liberty-springboot-jms/blob/master/README.md) in the Git repository.

To invoke the application simply find the base URL for the application in the Liberty messages.log e.g. `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms\-0.1.0](http://myzos.myompany.com:httpPort/com.ibm.cicsdev.springboot.jms-0.1.0`

1. Paste the base URL along with the REST service suffix `send/SPRING.QUEUE?data=I LOVE CICS` into the browser e.g. `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms\-0.1.0/send?data=I](http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-0.1.0/send/SPRING.QUEUE?data=I LOVE CICS`.  The browser will prompt for basic authentication. 

1. Next write the string "rollback" to the same queue using the REST service suffix `/send/SPRING.QUEUE?data=rollback`. 

1. Check if the specified TSQ has the information you expected by executing the CICS command `CEBR SPRINGQ`. For this example, you should just see one
"I LOVE CICS" in TSQ SPRINGQ as the second update should have rolled back. 



### Summary

Using JDBC to access relational databases is made easy in Spring using the JmsTemplate. 
After completing these tutorial, you should be able to start to build fully functional Java-based business applications in CICS using Spring Boot. 


### Additional resources


- [Spring Messaging with JMS](https://spring.io/guides/gs/messaging-jms/)
- [Spring JmsTemplate class](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jms/core/JmsTemplate.html)   
- CICS Tutorial - [Developing an MQ JMS application for CICS Liberty](https://github.com/cicsdev/cics-java-liberty-mq-jms/blob/master/blog.md)