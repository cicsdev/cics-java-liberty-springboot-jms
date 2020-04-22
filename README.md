# cics-java-liberty-springboot-jms

This sample project demonstrates a Spring Boot JMS application integrated with IBM CICS that can be deployed to a CICS Liberty JVM server. In this application, IBM MQ as the message broker which helps to send and receive messages between Spring Boot and CICS, and writes the messages into a CICS recoverable Temporary storage queue(TSQ). The jmsListener receives messages and writing CICS TSQ are under one transaction management. Both commit or both roll back if fails.

## Prerequisites

  - CICS TS V5.3 or later
  - A configured Liberty JVM server 
  - Java SE 1.8 or later on the z/OS system
  - Java SE 1.8 or later on the workstation
  - Either Gradle or Apache Maven on the workstation
  - IBM MQ V8.0 or later on z/OS
  - IBM MQ Resource Adapter for the WebSphere Application Server Liberty available from https://www.ibm.com/support/pages/node/489235

## Building 

You can choose to build the project using Gradle or Maven. The project includes both Gradle and Maven wrappers, these wrappers will automatically download required components from your chosen build tool; if not already present on your workstation.

You can also build the sample project through plug-in tooling of your chosen IDE. Both Gradle *buildship* and Maven *m2e* will integrate with Eclipse's "Run As..." capability allowing you to specify the required build-tasks. There are typically `clean bootWar` for Gradle and `clean package` for Maven, as reflected in the command line approach shown later.

**Note:** When building a WAR file for deployment to Liberty it is good practice to exclude Tomcat from the final runtime artifact. We demonstrate this in the pom.xml with the *provided* scope, and in build.gradle with the *providedRuntime()* dependency.

**Note:** If you import the project to an IDE of your choice, you might experience local project compile errors. To resolve these errors you should refresh your IDEs configuration. For example, in Eclipse: for Gradle, right-click on "Project", select "Gradle -> Refresh Gradle Project", or for Maven, right-click on "Project", select "Maven -> Update Project...".

### Gradle

Run the following in a local command prompt:

On Linux or Mac:

```shell
./gradlew clean bootWar
```
On Windows:

```shell
gradlew.bat clean bootWar
```

This creates a WAR file inside the `build/libs` directory.

### Maven


Run the following in a local command prompt:

On Linux or Mac:

```shell
./mvnw clean package
```

On Windows:

```shell
mvnw.cmd clean package
```

This creates a WAR file inside the `target` directory.


## Deploying

1. Ensure you have the following features in `server.xml`: 

    - *servlet-3.1* or *servlet-4.0*
    - *concurrent-1.0*
    - *jms-2.0* 
    - *wmqJmsClient-2.0* 
    - *jndi-1.0*
    - *cicsts:security-1.0* 
   
2. Add the JMS MQ Connection Factory configuration to `server.xml`
  
   Here's an example of configuration needed in `server.xml`: 

    ``` XML
    <!-- JMS MQ Connection Factory -->
    <jmsConnectionFactory id="jms/cf" jndiName="jms/cf">
        <properties.wmqJms channel="yourChannel" hostname="yourHost" port="yourPort" 
        queueManager="yourQueueManager" transportType="CLIENT"/>
        <connectionManager maxPoolSize="10" minPoolSize="0"/>
    </jmsConnectionFactory>
    <variable name="wmqJmsClient.rar.location" value="${server.config.dir}/wmq.jmsra-9.0.4.0.rar"/>

    ```

3. Copy and paste the WAR from your *target* or *build/libs* directory into a CICS bundle project and create a new WARbundlepart for that WAR file. 

4. Deploy the CICS bundle project as normal. For example in Eclipse, select "Export Bundle Project to z/OS UNIX File System".

5. Optionally, manually upload the WAR file to zFS and add an `<application>` configuration to server.xml:

``` XML
   <application id="com.ibm.cicsdev.springboot.jms-0.1.0"  
     location="${server.config.dir}/springapps/com.ibm.cicsdev.springboot.jms-0.1.0.war"  
     name="com.ibm.cicsdev.springboot.jms-0.1.0" type="war">
     <application-bnd>
        <security-role name="cicsAllAuthenticated">
            <special-subject type="ALL_AUTHENTICATED_USERS"/>
        </security-role>
     </application-bnd>  
   </application>
```

## Trying out the sample

1. Find the base URL for the application in the Liberty messages.log e.g.  `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-0.1.0`.

2. Past the base URL along with the REST service suffix 'send?data=I LOVE CICS' into the browser  e.g.  `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-0.1.0/send?data=I LOVE CICS`.  
The browser will prompt for basic authentication. Enter a valid userid and password - according to the configured registry for your target Liberty JVM server.

3. Check if the specified TSQ has the information you expected by executing the CICS command "CEBR SPRINGQ". For this example, you should just see one `I LOVE CICS` in TSQ SPRINGQ. The other one is roll backed because of meeting exceptions when receiving messages.

    
## License
This project is licensed under [Apache License Version 2.0](LICENSE). 
     

     