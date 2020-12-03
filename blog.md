
# Spring Boot Java applications for CICS, Part 5: JMS

## Introduction 

Spring Boot provides extensive support for integrating with messaging systems, from simplified use of the JMS API using JmsTemplate to a *message driven POJO* (MDP) to handle 
incoming asynchronous message using the `@JmsListener` annotation. 
This tutorial, the fifth in the “Spring Boot Java applications for CICS” series, demonstrates how to set up a Spring Boot JMS application integrated with CICS, build it with Maven or Gradle, 
and finally deploy and test it in a CICS Liberty JVM server using a IBM MQ series as the JMS provider. 

The application source and build scripts are available in at  cics\-java\-liberty\-springboot\-jms.


## Learning Objectives

This tutorial will show you how to:
1.  Create and build a Spring Boot application that uses JMS
2. 
3. 
4.  Test the sample in CICS

This is a web application and all requests can be made from a browser. The application uses the Spring Boot web interface to process GET REST requests. 
In a real-world implementation, other types of REST interfaces, such as POST, would be more appropriate. GET requests are used here for simplicity.

The application source and build scripts are available in the cicsdev/cics-java-liberty-springboot-jms repository.

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

### 1 Create the application


You can develop the code by following this tutorial step-by-step, or by downloading the [cics-java-liberty-springboot-jms]() example in GitHub.

If you are following along step-by-step, generate and download a Spring Boot web application using the Spring initializr website tool. 
For further details on how to do this, refer to part 1 of this tutorial series, (Spring Boot Java applications for CICS, Part 1: JCICS, Gradle, and Maven)[https://developer.ibm.com/technologies/java/tutorials/spring-boot-java-applications-for-cics-part-1-jcics-maven-gradle].
Eclipse is used as the preferred IDE.

Once your newly generated project has been imported into your IDE, you should have the `Application.java` and `ServletInitializer.java` classes which provide the basic framework of a Spring Boot web application.

In the first part of this tutorial series, we looked in-depth at how to use Gradle or Maven to build a Spring Boot web application for CICS. 
Using that knowledge, you should be in a position to enhance build.gradle or pom.xml to include dependencies on additional libraries. 
In particular, we require Spring Boot JMS support, support for JTA bean managed transactions, and support for JCICS. 


For Gradle, your build file should have the additional dependencies over and above the `spring-boot-starter-web`:
``` gradle    
    compileOnly enforcedPlatform("com.ibm.cics:com.ibm.cics.ts.bom:5.5-20200519131930-PH25409")
    compileOnly("com.ibm.cics:com.ibm.cics.server")          
    
    implementation("org.springframework.integration:spring-integration-jms")
    
    implementation("javax.transaction:javax.transaction-api")
    
    compileOnly("javax.jms:javax.jms-api")    
```

For Maven, you’ll need this additonal dependency in your pom.xml:

``` maven
    
    <dependency>
      <groupId>com.ibm.cics</groupId>
      <artifactId>com.ibm.cics.server</artifactId>
    </dependency>
  
    <dependency>
      <groupId>org.springframework.integration</groupId>
      <artifactId>spring-integration-jms</artifactId>
    </dependency>
   
    <dependency>
      <groupId>javax.jms</groupId>
      <artifactId>javax.jms-api</artifactId>
      <scope>provided</scope>
    </dependency>
   
    <dependency>
      <groupId>javax.transaction</groupId>
      <artifactId>javax.transaction-api</artifactId>
      <scope>provided</scope>
    </dependency>      

```

### 2 Send a simple JMS message


In this section, you’ll learn how to send a simple JMS message to an MQ queue using Spring’s `JmsTemplate`, and a JMS connection factory.

The first job is to update our Application class to create a Spring Bean which returns the JMS connection factory from the Liberty server configuration. 
We will use the name `jms/qcf1` for our connection factory. 
``` java

	private static final String CONNECTION_FACTORY = "jms/qcf1";	

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

Next we need to add a REST controller class, to provide a REST API to invoke the JMS send.
The root endpoint that provides usage details looks like this and also contains an `@Autowired` annotation for the JmsTemplate we will use later.
``` java
@RestController
public class JMSMessageSendController 
{
	@Autowired
	private JmsTemplate jmsTemplate;
	
    
    /**
     * Root endpoint - returns date/time + usage information
     * 
     * @return the Usage information 
     */	
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
```


JmsTemplate is the central class in Spring’s JMS core package. It simplifies the use of JMS and helps to avoid common errors. 
JmsTemplate executes the JMS operations leaving application code to provide the input and extract results.
The class a... 

We will add a new `send()` method to our REST controller, which takes as input the name of the JMS queue as a URI path variable, and the data to be written as a query string called "data". 
The format used will be `/send/{queue}?data={input string}`



``` java	

	@RequestMapping("/send/{jmsq}")
	public String send(@RequestParam(value = "data") String inputStr, @PathVariable String jmsq) 
	{
	   
	    try {
	        jmsTemplate.convertAndSend(jmsq, inputStr);
	    }
	    catch (JmsException jre) {
	        return "ERROR on JMS send " + jre.getMessage();   

	    }

	    return inputStr;
	}
```

We use the `convertAndSend()` method on the auto wired JmsTemplate to write the input string to the JMS queue specified. 
This method converts the input String object to a JMS message with a configured org.springframework.messaging.converter.MessageConverter). 
Need a bit more details on this.....



### 5 Deploy and run the sample

### 5.1 Configuring IBM MQ

Setup the following resources in the IBM MQ queue manager:

- MQ queue named DEMO.MDPQUEUE
- MQ queue named DEMO.SIMPLEQ

> Note: Queues should be defined as shareable to allow usage in the multi-threaded environment in Liberty. 
In addition it is advisable to set the `BackoutThreshold` attribute on the queue to a finite value, to prevent the MDP being constantly redriven if the MDP fails during the processing of the message.

### 5.2 Configuring CICS

-   servlet\-3.1 or servlet\-4.0(With servlet\-3.1 and WAR file Tomcat must be excluded and for servlet\-4.0 it doesn't matter.)
-   cicsts:security\-1.0
-   jms\-2.0
-   wmqJmsClient\-2.0
-   jndi\-1.0
-   concurrent\-1.0


``` xml
    <jmsQueueConnectionFactory connectionManagerRef="ConMgrJms" jndiname="jms/qcf1">
        <properties.wmqJms channel="<>" hostName="localhost" port="<>" queueManager="<>" transportType="CLIENT"/>
    </jmsQueueConnectionFactory>
	
	<variable name="wmqJmsClient.rar.location" value="/path/to/wmq/rar/wmq.jmsra.rar"/>
```
### Summary

### Additional resources


------------------------------


## Part 1:Create the Spring Boot application



Add SendJMSController.java as below:

```

/\*

 \* Copyright 2012\-2020 the original author or authors.

 \*

 \* Copyright IBM Corp. 2020 All Rights Reserved

 \*

 \* Licensed under the Apache License, Version 2.0 (the "License");

 \* you may not use this file except in compliance with the License.

 \* You may obtain a copy of the License at

 \*

\* [https://www.apache.org/licenses/LICENSE\-2.0](https://www.apache.org/licenses/LICENSE-2.0)

 \*

 \* Unless required by applicable law or agreed to in writing, software

 \* distributed under the License is distributed on an "AS IS" BASIS,

 \* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 \* See the License for the specific language governing permissions and

 \* limitations under the License.

 \*/

package com.ibm.cicsdev.springboot.jms;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jms.core.JmsTemplate;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;

/\*\*

 \* This class is to write a Rest Endpoint and use the JmsTemplate class to send

 \* the JMS messages.

 \*

 \* @RestController: build a Restful controller

 \* @Autowired: drive Dependency Injection

 \* @RequestMapping: write a Request URI method

 \*/

@RestController

public class SendJMSController {

    @Autowired

    private JmsTemplate jmsTemplate;

    @RequestMapping("/send")

    public String send(@RequestParam(value = "data") String data) {

        jmsTemplate.convertAndSend("BROWNAD.REQUEST.QUEUE", data);

        return data;

    }

}
```

After you copy and paste the above classes into the Maven project, you will see the compile errors. Please update your pom.xml as below:

```
<project xmlns="[http://maven.apache.org/POM/4.0.0"](http://maven.apache.org/POM/4.0.0%22) xmlns:xsi="[http://www.w3.org/2001/XMLSchema\-instance"](http://www.w3.org/2001/XMLSchema-instance%22)

  xsi:schemaLocation="[http://maven.apache.org/POM/4.0.0](http://maven.apache.org/POM/4.0.0) [http://maven.apache.org/xsd/maven\-4.0.0.xsd">](http://maven.apache.org/xsd/maven-4.0.0.xsd%22%3E)

  <modelVersion>4.0.0</modelVersion>

    <!\-\- Inherit defaults from Spring Boot \-\->

    <parent>

        <groupId>org.springframework.boot</groupId>

        <artifactId>spring\-boot\-starter\-parent</artifactId>

        <version>2.2.5.RELEASE</version>

    </parent>

    <groupId>com.ibm.cicsdev</groupId>

    <artifactId>com.ibm.cicsdev.springboot.jms</artifactId>

    <version>0.1.0</version>

     <!\-\- The project produces a war file rather than a jar file \-\->

    <packaging>war</packaging>

    <name>com.ibm.cicsdev.springboot.jms</name>

    <description>Demo project for Spring Boot</description>

    <properties>

        <java.version>1.8</java.version>

    </properties>

  <!\-\- Package as an executable war(default jar) \-\->

  <build>

        <plugins>

            <plugin>

                <groupId>org.springframework.boot</groupId>

                <artifactId>spring\-boot\-maven\-plugin</artifactId>

            </plugin>

        </plugins>

  </build>

 <!\-\- CICS BOM (as of 21st Nov 2019) \-\->

  <dependencyManagement>

      <dependencies>

        <dependency>

          <groupId>com.ibm.cics</groupId>

          <artifactId>com.ibm.cics.ts.bom</artifactId>

          <version>5.5\-20191121085445\-PH14856</version>

          <type>pom</type>

          <scope>import</scope>

         </dependency>

      </dependencies>

   </dependencyManagement>

<dependencies>

 <!\-\- Don't include JCICS in the final build (no need for version because we have BOM)  \-\->

   <dependency>

     <groupId>com.ibm.cics</groupId>

     <artifactId>com.ibm.cics.server</artifactId>

   </dependency>

<!\-\- Spring web support \-\->

   <dependency>

     <groupId>org.springframework.boot</groupId>

     <artifactId>spring\-boot\-starter\-web</artifactId>

   </dependency>

<!\-\- Spring Integration JMS Support  \-\->

   <dependency>

            <groupId>org.springframework.integration</groupId>

            <artifactId>spring\-integration\-jms</artifactId>

   </dependency>

<!\-\-  For JMS API \-\->

   <dependency>

          <groupId>javax.jms</groupId>

          <artifactId>javax.jms\-api</artifactId>

          <scope>provided</scope>

   </dependency>

<!\-\- For Bean managed transactions \-\->

   <dependency>

          <groupId>javax.transaction</groupId>

          <artifactId>javax.transaction\-api</artifactId>

          <scope>provided</scope>

   </dependency>

<!\-\- Don't include TomCat in the runtime build \-\->

   <dependency>

            <groupId>org.springframework.boot</groupId>

            <artifactId>spring\-boot\-starter\-tomcat</artifactId>

            <scope>provided</scope>

        </dependency>

  </dependencies>

</project>

If you still see the compile errors, right click your Project and click **Maven**\-> **Update Project...**

If you use Gradle, please update your build.gradle as below:

plugins

{

id 'org.springframework.boot' version '2.2.5.RELEASE'

id 'io.spring.dependency\-management' version '1.0.9.RELEASE'

id 'java'

id 'eclipse'

id 'idea'

id 'war'

}

group = 'com.ibm.cicsdev.springboot'

archivesBaseName='com.ibm.cicsdev.springboot.jms'

version = '0.1.0'

sourceCompatibility = '1.8'

// If in Eclipse, add Javadoc to the local project classpath

eclipse

{

    classpath

    {

        downloadJavadoc = true

    }

}

repositories {

    mavenCentral()

}

dependencies {

    implementation("org.springframework.boot:spring\-boot\-starter\-web")

    // Don't include TomCat in the runtime build, but do put it in WEB\-INF so it can be run standalone a well as embedded

    providedRuntime("org.springframework.boot:spring\-boot\-starter\-tomcat")

    // CICS BOM (as of 21st Nov 2019)

    compileOnly enforcedPlatform("com.ibm.cics:com.ibm.cics.ts.bom:5.5\-20191121085445\-PH14856")

    // Don't include JCICS in the final build (no need for version because we have BOM)

    compileOnly("com.ibm.cics:com.ibm.cics.server")

    // Spring Integration JMS Support

    implementation("org.springframework.integration:spring\-integration\-jms")

    // For JTA Bean managed transactions (this is newer and covers up to jta\-1.3)

    implementation("javax.transaction:javax.transaction\-api")

    // For JMS API

    compileOnly("javax.jms:javax.jms\-api")

}
```

If you still see the compile errors, right click your Project and click **Gradle**\-> **Refresh Gradle Project.**

With the Java source expanded the project now should look similar to the following:

<insert graphic>

To avoid the warnings about using BLANK and default CICS userid, you need to include a simple web.xml to give basic authentication. Please store this web.xml in src/main/webapp/WEB\-INF. Code as below:

```
<?xml version="1.0" encoding="UTF\-8"?>
<web\-app xmlns="[http://java.sun.com/xml/ns/javaee"](http://java.sun.com/xml/ns/javaee%22) xmlns:xsi="[http://www.w3.org/2001/XMLSchema\-instance"](http://www.w3.org/2001/XMLSchema-instance%22) xsi:schemaLocation="[http://java.sun.com/xml/ns/javaee](http://java.sun.com/xml/ns/javaee) [http://java.sun.com/xml/ns/javaee/web\-app\_3\_0.xsd"](http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd%22) version="3.0">

    <display\-name>cics\-java\-liberty\-springboot\-jms</display\-name>

<login\-config>

        <auth\-method>BASIC</auth\-method>

    </login\-config>

    <security\-constraint>

        <display\-name>com.ibm.cicsdev.springboot.jms</display\-name>

        <web\-resource\-collection>

            <web\-resource\-name>com.ibm.cicsdev.springboot.jms</web\-resource\-name>

            <description>Protection rules for all servlets</description>

            <url\-pattern>/\*</url\-pattern>

        </web\-resource\-collection>

        <auth\-constraint>

            <description>All Authenticated users </description>

            <role\-name>cicsAllAuthenticated</role\-name>

        </auth\-constraint>

    </security\-constraint>

    <security\-role>

        <description>The CICS cicsAllAuthenticated role</description>

        <role\-name>cicsAllAuthenticated</role\-name>

    </security\-role>

</web\-app>
```

You can build your application to a WAR by using Gradle or Maven.

## Part 2: Detailed introduction of the application

1) IBM MQ as the message broker which helps to send and receive messages between Spring Boot and CICS.

If we want to send messages to IBM MQ from Spring Boot application, we must first create a JMS MQ connection factory.

From CICS Liberty side, in the server.xml, define the connection factory referencing the MQ queue manager and channel, and the TCP/IP host and port on which the MQ channel is listening. In addition add a connectionManager definition to define the size of the connection manager pool.

```
<!\-\- JMS MQ Connection Factory \-\->

  <jmsConnectionFactory id="jms/cf" jndiName="jms/cf" connectionManagerRef="ConMgrJms">

    <properties.wmqJms channel="yourChannel"

                       hostname="yourHost"

                       port="yourPort"

                       queueManager="yourQueueManager"

                       transportType="CLIENT" />

  </jmsConnectionFactory>

  <connectionManager id="ConMgrJms" maxPoolSize="10" minPoolSize="0"/>
  ```

2) Update the Application.java to add the necessary configurations. Adding the below bean, Spring can look up the JMS ConnectionFactory using JNDI directly from CICS Liberty's server.xml configuration in step 1).

```
**private**  **static**  **final** String ***CONNECTION\_FACTORY*** = "jms/cf";

 [@Bean](https://jazz104.hursley.ibm.com:9443/jazz/users/Bean)

 **public** ConnectionFactory connectionFactory() {

 **try** {

            // Look up the connection factory from Liberty

            ConnectionFactory fact = InitialContext. *doLookup*(***CONNECTION\_FACTORY*** );

 **return** fact;

} **catch**(NamingException e) {

            e.printStackTrace();

 **return**  **null**;

        }

    }
```

3)Till now you can then use the *JmsTemplate* class to send/receive JMS messages, which will use the *ConnectionFactory* to connect the message broker(IBM MQ). Spring uses JmsTemplate to simply the development of JMS message handling. Application developers can focus on send and receive messages. Other things such as create a connection, get a session and how to send/receive messages can rely on JmsTemplate. Here we use convertAndSend() method which can send the given object to the specified destination, converting the object to a JMS message with a configured MessageConverter(org.springframework.messaging.converter.MessageConverter). Except the *ConnectionFactory,* message destination is also very important. We must know where the message send to or receive from. Here "myQueueDestination" is the queue destination.


```
[@Autowired](https://jazz104.hursley.ibm.com:9443/jazz/users/Autowired)

 **private** JmsTemplate jmsTemplate;

 [@RequestMapping](https://jazz104.hursley.ibm.com:9443/jazz/users/RequestMapping) ("/send")

 **public** String send([@RequestParam](https://jazz104.hursley.ibm.com:9443/jazz/users/RequestParam) (value="data") String data) {

        jmsTemplate.convertAndSend("myQueueDestination", data);

 **return** data;

    }
 ```

4) Although the method receive() of JmsTemplate can receive messages, it has to be performed in a  synchronous way. Before the message arrive s, the application can't do other things, and just has to wait. However, similar to Java EE message driven beans, Spring offers an asynchronous message driven processing model called a *message driven POJO*  (MDP) to handle incoming asynchronous messages. To support this we need to config a JmsListenerContainerFactory which depends on the connectionFactory. The jmsListenerContainer can monitor the JMS destination. It will accept the messages from the JMS destination and send to the related JmsListener. So you can  annotate a method [*@JmsListener*](https://jazz104.hursley.ibm.com:9443/jazz/users/JmsListener) *(destination="*myQueueDestination*" connectionFactory="myFactory")* to receive messages.

```
[@Bean](https://jazz104.hursley.ibm.com:9443/jazz/users/Bean)

 **public** JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory) {

DefaultJmsListenerContainerFactory factory = **new** DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);

 **return** factory;

    }

[@JmsListener](https://jazz104.hursley.ibm.com:9443/jazz/users/JmsListener) (destination = "myQueueDestination", containerFactory = "myFactory")

 **public**  **void** receiveMessage(String data) **throws** CicsException {

        System.***out***.println("Received <" + data + ">");

}
```

5) Now we can both send and receive a JMS message from our Spring Boot application using IBM MQ. However,  incoming messages to the JmsListenerContainerFactory won't be able to use the JCICS API as requests to the JmsListenerContainerFactory by default don't run on a CICS enabled thread. If a request tries to use the JCICS API, you will get the error below:

```
"*org.springframework.jms.listener.adapter.ListenerExecutionFailedException: Listener method 'public void com.ibm.cicsdev.springboot.jms.smokeTest.JMSMessageReceiver.receiveMessage(java.lang.String) throws com.ibm.cics.server.CicsException' threw exception; nested exception is com.ibm.cics.server.CicsRuntimeException: DTCTSQ\_WRITE: No JCICS context is associated with the current thread.* "
```

6) If you want to use a JmsListener with a CICS\-enabled thread, you'll need to bind a *TaskExecutor* to the *JmsListenerContainerFactory*. For this you'll first need the following Liberty features installed:


- jndi\-1.0
- concurrent\-1.0

You'll need to specify that Spring should use the TaskExecutor based on the ManagedExecutorService provided by Liberty over JNDI in the Application/AppConfig, so that it uses threads from the Liberty executor (which are CICS enabled by default).

```
[@Bean](https://jazz104.hursley.ibm.com:9443/jazz/users/Bean)

 **public** TaskExecutor taskExecutor()

    {

 **return**  **new** DefaultManagedTaskExecutor();

    }
 ```

Now you need to set `factory.setTaskExecutor(taskExecutor())` on the `JmsListenerContainerFactory` as below:

```

 [@Bean](https://jazz104.hursley.ibm.com:9443/jazz/users/Bean)

 **public** JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory) {

DefaultJmsListenerContainerFactory factory = **new** DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);

        factory.setTaskExecutor(taskExecutor());

 **return** factory;

    }
```

7) Spring JMS transaction management

Please look at the below code, after receiving a message, the application writes the data from the message to a CICS temporary storage queue (TSQ) "SPRINGQ" . However, after these two steps, it throws a RuntimeException, which will by default cause the Spring transaction to rollback. However, if you run this code you will note that even if the TSQ is r ecoverable it does not rollback when the exception is thrown, meaning the two operations are not in the same recoverable transaction scope.

```
[@JmsListener](https://jazz104.hursley.ibm.com:9443/jazz/users/JmsListener) (destination = "myQueueDestination ", containerFactory = "myFactory")

 **public**  **void** receiveMessage(String data) **throws** CicsException {

        System. ***out***.println("Received <" + data + ">");

TSQ tsq = **new** TSQ();

        tsq.setName("SPRINGQ");

        tsq.writeString(data);

        // Let us check if tsq SPRINGQ can be rollbacked

        System. ***out***.println("Check if SPRINGQ Rolling back");

 **throw**  **new** RuntimeException("Expected exception");

}
```

How can we make receiving the message and accessing CICS resources in to one recoverable transaction?

Spring supplies a transaction management infrastructure for helping to automatically commit or roll back transactions if they fail. PlatformTransactionManager is the central interface in Spring's transaction infrastructure. Spring makes available several implementations of the platform transaction manager interface for handling transactions across JDBC, JPA, Hibernate, JMS and so on.

For JMS, Spring uses JmsTransactionManager which  implements PlatformTransactionManager interface to do the transaction management for the JMS ConnectionFactory.  It binds a JMS Connection/Session pair from the specified ConnectionFactory to the thread, potentially allowing for one thread\-bound Session per ConnectionFactory. This local strategy is an alternative to executing JMS operations within JTA transactions. Its advantage is that it is able to work in any environment, for example a standalone application or a test suite, with any message broker as target. However, this strategy is *not* able to provide XA transactions, for example in order to share transactions between messaging and database access. A full JTA configuration is required for XA transactions, typically using Spring's JtaTransactionManager as the strategy. So for CICS with Spring JMS, in order to integrate recoverable operations across our JMSListner and CICS resource we should use the Spring JtaTransactionManager to manage the transaction. The JtaTransactionManager is portable across all Java EE servers and corresponds to the functionality of the JTA UserTransaction, for which Java EE specifies a standard JNDI name ("java:comp/UserTransaction").

So based on the above knowledge, we should add a new Spring @Bean annotation as below, it will define a *PlatformTransactionManager* returning the *JtaTransactionManager.*

```

@Bean
public PlatformTransactionManager platformTransactionManager(ConnectionFactory connectionFactory) {

        //return new JmsTransactionManager(connectionFactory);

 try {

            UserTransaction tx = InitialContext. *doLookup*("java:comp/UserTransaction");

 return  **new** JtaTransactionManager(tx);

} catch (NamingException e) {

            e.printStackTrace();

 return  null;

        }

    }
```

Then we need to bind this transaction manager to the JmsListenerContainerFactory like below:

```
factory.setTransactionManager(platformTransactionManager(connectionFactory));
```

After the above configuration, the jmsListener receives messages and CICS resource access will be under one global transaction , meaning both operations commit or both rollback if an Exception is thrown.

How to commit or rollback transaction? Spring uses two methods to manage transaction. One is programatic, the other is declarative transaction with the [@Transactional](https://jazz104.hursley.ibm.com:9443/jazz/users/Transactional) annotation, add [@EnableTransactionManagement](https://jazz104.hursley.ibm.com:9443/jazz/users/EnableTransactionManagement) to your configuration. The annotation [@EnableTransactionManagement](https://jazz104.hursley.ibm.com:9443/jazz/users/EnableTransactionManagement)   will automatically scan your beans and look for a bean of type PlatformTransactionManager, which it will use. Then, in your Java beans, simply declare methods as transactional by annotating them using the  [@Transactional](https://jazz104.hursley.ibm.com:9443/jazz/users/Transactional) annotation. Most Spring Framework users choose declarative transaction management as tis option has the least impact on application code. In our article, we choose to use [@Transactional](https://jazz104.hursley.ibm.com:9443/jazz/users/Transactional) annotation on the method MDP receiveMessage().


## Part 3: Deploy the WAR to a CICS Liberty JVM server

There are several ways to deploy the WAR. You can add an <application> element which points to your uploaded WAR file location (about the configuration, please check the GitHub sample). Or you can use a CICS bundle. In this article, we will introduce how to deploy the Spring Boot WAR as a WAR bundlepart with a CICS bundle.

a) Create a new CICS Bundle Project with id "com.ibm.cicsdev.springboot.jms.cicsBundle"

<insert graphic>

Copy the WAR file into this CICS Bundle Project, and add a .warbundle file

<insert graphic>

The content of the warbundle file is as below:

```
<?xml version="1.0" encoding="UTF\-8" standalone="yes"?>

<warbundle symbolicname="com.ibm.cicsdev.springboot.jms\-0.1.0" jvmserver="YourJVMServerName"/>
```

b) Right\-click the CICS Bundle Project and then click Export Bundle Project to z/OS UNIX File System

c) Create a new CICS Bundle named "JMSSPG" like this:

<insert graphic>

d) Define and install TSMODEL

To check if CICS resource can be roll backed when failing to receive  messages, we should make CICS TSQ recoverable. To do this you need to define a CICS TSMODEL resource with **Prefix set to SPRINGQ**, and with **Recovery set to Yes**, and then install the resource.

For example:

<insert graphic>

e) Start and enable your CICS Liberty JVM server. If you don't yet have a Liberty JVM server configured, using CICS auto\-configuration is a great way to start. If you enable auto\-configuration in the JVM profile, it will generate a basic server.xml when the JVM server is enabled. For more information, see [Configuring a Liberty JVM server](https://www.ibm.com/support/knowledgecenter/en/SSGMCP_5.5.0/configuring/java/config_jvmserver_liberty.html) in the CICS Knowledge Center.

If you're customising an existing configuration, you'll need to make sure you include the following feature:

-   servlet\-3.1 or servlet\-4.0(With servlet\-3.1 and WAR file Tomcat must be excluded and for servlet\-4.0 it doesn't matter.)
-   cicsts:security\-1.0
-   jms\-2.0
-   wmqJmsClient\-2.0
-   jndi\-1.0
-   concurrent\-1.0

f) Install and enable the CICS Bundle "JMSSPG" you created in step c).

g) Check message.log to see if the Spring Boot application deployed successfully.

```
A CWWKT0016I: Web application available (default\_host): http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms\-0.1.0
I SRVE0292I: Servlet Message \- \[com.ibm.cicsdev.springboot.j ms\- 0.1.0\]:.Initializing Spring embedded WebApplicationContext
```

## Part 4: Test this Spring Boot application

1.  Find the base URL for the application in the Liberty messages.log e.g. [http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms\-0.1.0](http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-0.1.0) .

2.  Past the base URL along with the REST service suffix 'send?data=I LOVE CICS' into the browser  e.g. [http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms\-0.1.0/send?data=I](http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-0.1.0/send?data=I) LOVE CICS.  The browser will prompt for basic authentication. Enter a valid userid and password \- according to the configured registry for your target Liberty JVM server.

3.  Check if the specified TSQ has the information you expected by executing the CICS command "CEBR SPRINGQ". For this example, you should just see one I LOVE CICS in TSQ SPRINGQ. The other one is roll backed because of meeting exceptions when receiving messages.
