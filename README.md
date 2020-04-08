# cics-java-liberty-springboot-jms

This sample project demonstrates a Spring Boot JMS application integrated with IBM CICS that can be deployed to a CICS Liberty JVM server. In this application, IBM MQ as the message broker which helps to send and receive messages between Spring Boot and CICS, and writes the messages into a CICS recoverable Temporary storage queue(TSQ). The jmsListener receives messages and writing CICS TSQ are under one transaction management. Both commit or both roll back if fails.

For more information, see blog post - link TBC.

## Prerequisites

  - CICS TS V5.5 or later
  - A configured Liberty JVM server 
  - Java SE 1.8 or later on the z/OS system
  - Java SE 1.8 or later on the workstation
  - Either Gradle or Apache Maven on the workstation
  - IBM MQ V8.0 or later on z/OS
  - IBM MQ Resource Adapter for the WebSphere Application Server Liberty available from https://www.ibm.com/support/pages/node/489235

## Building 

You can choose to build the project using Gradle or Maven. They will produce the same results. The project includes the Gradle and Maven wrappers, which will automatically download the correct version of the tools if not present on your workstation.

Notice: After you import the project to your IDE, such as Eclipse, if you choose Maven, please right-click on Project, select Maven -> "Update Project..." to fix the compile errors; if you choose Gradle, please right-click on Project, select Gradle -> "Refresh Gradle Project" to fix the compile errors.

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

1. Transfer the WAR file to zFS for example using FTP.  

2. Ensure you have the following features in `server.xml`:

    - servlet-3.1 
    - concurrent-1.0
    - jms-2.0 
    - wmqJmsClient-2.0 
    - jndi-1.0 
    - cicsts:security-1.0 
    
3. Add the JMS MQ Connection Factory configuration to `server.xml`
  
   Here's an example of configuration needed in `server.xml`: 

    ```
    <!-- JMS MQ Connection Factory -->
    <jmsConnectionFactory id="jms/cf" jndiName="jms/cf">
        <properties.wmqJms channel="yourChannel" hostname="yourHost" port="yourPort" queueManager="yourQueueManager" transportType="CLIENT"/>
        <connectionManager maxPoolSize="10" minPoolSize="0"/>
    </jmsConnectionFactory>
    <variable name="wmqJmsClient.rar.location" value="${server.config.dir}/wmq.jmsra-9.0.4.0.rar"/>

    ```

4. Copy and paste WAR from build project into a CICS bundle project and create WARbundlepart. Deploy the Spring Boot application by this CICS bundle. 

5. Notice: About the MQ resource adapter(wmq.jmsra-9.0.4.0.rar in this sample), you can refer to the official download on FixCentral https://www.ibm.com/support/pages/node/489235


## Trying out the sample

1. Find the URL for the application in messages.log e.g. `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-1.0.0`. 

2. From the browser you can visit the URL:`http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-1.0.0/send?data=I LOVE CICS`.
Then you will find the browser prompts for a basic authentication, please type your userid and password.  

3. Check if the specified TSQ has the information you expected by executing the CICS command "CEBR SPRINGQ". For this example, you should just see one `I LOVE CICS` in TSQ SPRINGQ. The other one is roll backed because of meeting exceptions when receiving messages.
    
## License
This project is licensed under [Apache License Version 2.0](LICENSE). 
     