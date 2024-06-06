# cics-java-liberty-springboot-jms
[![Build](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/java.yaml/badge.svg)](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/java.yaml)

This sample project demonstrates how to use the Spring Boot JMS template to integrate with CICS and use IBM MQ as the message provider. The sample is intended for deployment to a CICS Liberty JVM server. 

Invoking the REST end-point of the application will write a message with the data you provide to MQ. A JMS listener endpoint receives the message from MQ and writes it to a CICS Temporary storage queue (TSQ). Reading from the JMS queue and writing to the CICS TSQ are performed within the same transaction using JTA via a Spring Boot container managed transaction, ensuring everything commits or rolls back as one recoverable unit.

For further details about the development of this sample refer to the IBM developer tutorial [Spring Boot Java applications for CICS, Part 5: JMS](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-5-jms/)

## Prerequisites

- CICS TS V5.3 or later
- A configured Liberty JVM server 
- Java SE 1.8 or later on the workstation
- An Eclipse development environment on the workstation (optional)
- Either Gradle or Apache Maven on the workstation
- IBM MQ V8.0 or later on z/OS
- IBM MQ Resource Adapter for the WebSphere Application Server Liberty available from https://www.ibm.com/support/pages/node/489235

## Downloading

- Clone the repository using your IDEs support, such as the Eclipse Git plugin
- **or**, download the sample as a [ZIP](https://github.com/cicsdev/cics-java-liberty-springboot-jms/archive/main.zip) and unzip onto the workstation

>*Tip: Eclipse Git provides an 'Import existing Projects' check-box when cloning a repository.*

### Check dependencies
 
Before building this sample, you should verify that the correct CICS TS bill of materials (BOM) is specified for your target release of CICS. The BOM specifies a consistent set of artifacts, and adds information about their scope. In the example below the version specified is compatible with CICS TS V5.5 with JCICS APAR PH25409, or newer. That is, the Java byte codes built by compiling against this version of JCICS will be compatible with later CICS TS versions and subsequent JCICS APARs. 

You can browse the published versions of the CICS BOM at [Maven Central](https://mvnrepository.com/artifact/com.ibm.cics/com.ibm.cics.ts.bom).
 
Gradle (build.gradle): 

`compileOnly enforcedPlatform("com.ibm.cics:com.ibm.cics.ts.bom:5.5-20200519131930-PH25409")`

Maven (POM.xml):

``` xml	
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.ibm.cics</groupId>
      <artifactId>com.ibm.cics.ts.bom</artifactId>
      <version>5.5-20200519131930-PH25409</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Updating

The JMSMessageReceiver.java class assumes an MQ destination of `SPRING.QUEUE`. If that value does not meet your enterprise naming standards, this can be updated by modifying the static variable `MDP_QUEUE`.

## Building 

You can build the sample using an IDE of your choice, or you can build it from the command line. For both approaches, using the supplied Gradle or Maven wrapper is the recommended way to get a consistent version of build tooling. 

On the command line, you simply swap the Gradle or Maven command for the wrapper equivalent, `gradlew` or `mvnw` respectively.

For an IDE, taking Eclipse as an example, the plug-ins for Gradle *buildship* and Maven *m2e* will integrate with the "Run As..." capability, allowing you to specify whether you want to build the project with a Wrapper, or a specific version of your chosen build tool.

The required build-tasks are typically `clean bootWar` for Gradle and `clean package` for Maven. Once run, Gradle will generate a WAR file in the `build/libs` directory, while Maven will generate it in the `target` directory.

**Note:** When building a WAR file for deployment to Liberty it is good practice to exclude Tomcat from the final runtime artifact. We demonstrate this in the pom.xml with the *provided* scope, and in build.gradle with the *providedRuntime()* dependency.

**Note:** If you import the project to your IDE, you might experience local project compile errors. To resolve these errors you should run a tooling refresh on that project. For example, in Eclipse: right-click on "Project", select "Gradle -> Refresh Gradle Project", **or** right-click on "Project", select "Maven -> Update Project...".

>Tip: *In Eclipse, Gradle (buildship) is able to fully refresh and resolve the local classpath even if the project was previously updated by Maven. However, Maven (m2e) does not currently reciprocate that capability. If you previously refreshed the project with Gradle, you'll need to manually remove the 'Project Dependencies' entry on the Java build-path of your Project Properties to avoid duplication errors when performing a Maven Project Update.* 

#### Gradle Wrapper (command line)

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

#### Maven Wrapper (command line)

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

### IBM MQ

You will need to configure
- An MQ queue manager listening on a accessible TCP/IP port.
- A queue named SPRING.QUEUE

> Note: Queues should be defined as shareable to allow usage in the multi-threaded environment in Liberty. 
In addition it is advisable to set the `BackoutThreshold` attribute on the queue, to prevent the MDP being constantly re-driven if the MDP fails during the processing of the message.

### CICS Liberty

- Ensure you have the following features in `server.xml`: 
    - `servlet-3.1` or `servlet-4.0` depending on the version of Java EE in use.
    - `concurrent-1.0`.
    - `jms-2.0`.
    - `wmqJmsClient-2.0`.
    - `jndi-1.0`.
    - `cicsts:security-1.0` if CICS security is enabled. 

>**Note:** `servlet-4.0` will only work for CICS TS V5.5 or later

- Add the JMS MQ Connection Factory configuration to `server.xml`

  Here's an example of configuration needed in `server.xml`. Substitute the *channel*, *hostname*, *port* and *queueManager* values to your installation values, and fill in the MQ rar location value with the location on zFS to which you downloaded the rar. Download instructions can be found linked from the prerequisites section earlier: 

    ``` XML
    <!-- JMS MQ Connection Factory -->
    <jmsConnectionFactory id="jms/cf" jndiName="jms/cf">
        <properties.wmqJms channel="yourChannel" hostname="yourHost" port="yourPort" 
        queueManager="yourQueueManager" transportType="CLIENT"/>
        <connectionManager maxPoolSize="10" minPoolSize="0"/>
    </jmsConnectionFactory>
    <variable name="wmqJmsClient.rar.location" value="/wmq.jmsra-9.0.4.0.rar"/>
    ```

The value of 10 on `maxPoolSize` is used as an example only. Set `maxPoolSize` to the maximum number of concurrent users of the connection factory.
 
- Deployment option 1:
    - Copy and paste the built WAR from your *target* or *build/libs* directory into a Eclipse CICS bundle project and create a new WAR bundlepart that references the WAR file. Then deploy the CICS bundle project from CICS Explorer using the **Export Bundle Project to z/OS UNIX File System** wizard.

- Deployment option 2:
    - Manually upload the WAR file to zFS and add an `<application>` element to the Liberty server.xml to define the web application with access to all authenticated users. For example the following application element can be used to install a WAR, and grant access to all authenticated users if security is enabled.

    ``` XML
    <application id="cics-java-liberty-springboot-jms-0.1.0"
        location="${server.config.dir}/springapps/cics-java-liberty-springboot-jms-0.1.0.war"
        name="cics-java-liberty-springboot-jms-0.1.0" type="war">
        <application-bnd>
            <security-role name="cicsAllAuthenticated">
                <special-subject type="ALL_AUTHENTICATED_USERS"/>
            </security-role>
        </application-bnd>
    </application>
    ```

## Trying out the sample

1. Ensure the web application started successfully in Liberty by checking for msg `CWWKT0016I` in the Liberty messages.log:

    - `A CWWKT0016I: Web application available (default_host): http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jms-0.1.0`
    - `I SRVE0292I: Servlet Message - [com.ibm.cicsdev.springboot.jms-0.1.0]:.Initializing Spring embedded WebApplicationContext`

2. Copy the context root from message CWWKT0016I along with the REST service suffix `send/SPRING.QUEUE?data=I LOVE CICS` into you web browser. e.g. `http://myzos.mycompany.com:httpPort/com.ibm.cicsdev.springboot.jms-0.1.0/send/SPRING.QUEUE?data=I LOVE CICS`.

3. Check if the specified TSQ has the information you expected by executing the CICS 3270 command `CEBR SPRINGQ`. For this example, you should just see one `I LOVE CICS` in TSQ SPRINGQ. 

4. Check that the Spring MDP Bean has been driven by viewing the messages in the Liberty messages.log


## License

This project is licensed under [Eclipse Public License - v 2.0](LICENSE). 
