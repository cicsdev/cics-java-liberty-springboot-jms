# cics-java-liberty-springboot-jms
[![Build](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml/badge.svg)](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml)
[![License](https://img.shields.io/badge/License-EPL%202.0-green.svg)](https://www.eclipse.org/legal/epl-2.0/)

## Overview

This sample project demonstrates how to use the Spring Boot JMS template to integrate with CICS and use IBM MQ as the message provider. The sample is intended for deployment to a CICS Liberty JVM server.

Invoking the REST endpoint of the application will write a message with the data you provide to MQ. A JMS listener endpoint receives the message from MQ and writes it to a CICS Temporary Storage Queue (TSQ). Reading from the JMS queue and writing to the CICS TSQ are performed within the same transaction using JTA via a Spring Boot container managed transaction, ensuring everything commits or rolls back as one recoverable unit.

**Key Features:**
- **Spring JMS Integration**: Uses `spring-integration-jms` to send and receive JMS messages via IBM MQ
- **Transactional Messaging**: JTA container-managed transaction spans both MQ read and CICS TSQ write
- **RESTful Trigger**: REST endpoint triggers message send; JMS listener drives TSQ write
- **Multi-Build Support**: Compatible with both Gradle and Maven

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Reference](#reference)
4. [Downloading](#downloading)
5. [Check Dependencies](#check-dependencies)
6. [Building the Sample](#building-the-sample)
7. [Deploying to a CICS Liberty JVM server](#deploying-to-a-cics-liberty-jvm-server)
8. [Running the Sample](#running-the-sample)
9. [Troubleshooting](#troubleshooting)
10. [License](#license)
11. [Additional Resources](#additional-resources)
12. [Contributing](#contributing)

## Prerequisites

- CICS TS V6.1 or later (required for Spring Boot 3.x and Jakarta EE 10 support)
- A configured Liberty JVM server in CICS
- Java SE 17 or later on the workstation
- An Eclipse development environment on the workstation (optional)
- Either Gradle or Apache Maven on the workstation (optional if using Wrappers)
- IBM MQ V8.0 or later on z/OS
- IBM MQ Resource Adapter for WebSphere Application Server Liberty, available from https://www.ibm.com/support/pages/node/489235

## Reference

For more information about the development of this sample, see [Spring Boot Java applications for CICS, Part 5: JMS](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-5-jms/).

## Downloading

**If using Eclipse:** the simplest approach is to clone the repository using the Eclipse Git plugin (EGit) perspective.

**If using the command line:**
```shell
git clone https://github.com/cicsdev/cics-java-liberty-springboot-jms
```
Alternatively, download the sample as a [ZIP](https://github.com/cicsdev/cics-java-liberty-springboot-jms/archive/main.zip) and unzip onto the workstation.

**If importing into Eclipse:**
1. In the **Git Repositories** view, right-click the repository → **Import as Project** (imports the root project)
   *(if you cloned from the command line, use **File → Import → Existing Projects into Workspace** instead, browse to the cloned directory, select all projects, and skip to step 6)*
2. Switch to the **Java EE** perspective
3. In the **Project Explorer**, right-click the `cics-java-liberty-springboot-jms-app` folder → **Import as Project**
4. Right-click the `cics-java-liberty-springboot-jms-cicsbundle` folder → **Import as Project**
5. Right-click the `cics-java-liberty-springboot-jms-cicsbundle-eclipse` folder → **Import as Project**
6. **Required:** Right-click the root project → **Gradle → Refresh Gradle Project** or **Maven → Update Project...** — this resolves Spring Boot and CICS dependencies into the project classpath. Without this step, the WTP export will produce an incomplete WAR missing `WEB-INF/lib`.

### Check dependencies

Before building this sample, you should verify that the correct CICS TS bill of materials (BOM) is specified for your target release of CICS. The BOM specifies a consistent set of artifacts, and adds information about their scope. In the example below the version specified is compatible with CICS TS V6.1 with JCICS APAR PH63856, or newer. That is, the Java byte codes built by compiling against this version of JCICS will be compatible with later CICS TS versions and subsequent JCICS APARs.

You can browse the published versions of the CICS BOM at [Maven Central.](https://mvnrepository.com/artifact/com.ibm.cics/com.ibm.cics.ts.bom)

Gradle (build.gradle):

`compileOnly enforcedPlatform("com.ibm.cics:com.ibm.cics.ts.bom:6.1-20250812133513-PH63856")`

Maven (POM.xml):

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.ibm.cics</groupId>
      <artifactId>com.ibm.cics.ts.bom</artifactId>
      <version>6.1-20250812133513-PH63856</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

## Building the Sample

You can build the sample using an IDE of your choice, or you can build it from the command line. For both approaches, using the supplied Gradle or Maven wrapper is the recommended way to get a consistent version of build tooling.

On the command line, you simply swap the Gradle or Maven command for the wrapper equivalent, `gradlew` or `mvnw` respectively.

For an IDE, taking Eclipse as an example, the plug-ins for Gradle *buildship* and Maven *m2e* will integrate with the "Run As..." capability, allowing you to specify whether you want to build the project with a Wrapper, or a specific version of your chosen build tool.

The required build-tasks are `clean build` for Gradle and `clean verify` for Maven. Once run, Gradle will generate a WAR file in the `cics-java-liberty-springboot-jms-app/build/libs` directory, while Maven will generate it in the `cics-java-liberty-springboot-jms-app/target` directory.

**Note:** When building a WAR file for deployment to Liberty it is good practice to exclude Tomcat from the final runtime artifact. We demonstrate this in the pom.xml with the *provided* scope, and in build.gradle with the *providedRuntime()* dependency.

### Gradle Wrapper (command line)

Run the following in a local command prompt:

On Linux or Mac:

```shell
./gradlew clean build
```

On Windows:

```shell
gradlew.bat clean build
```

This creates a WAR file inside the `cics-java-liberty-springboot-jms-app/build/libs` directory.

> **Note:** In Eclipse, the `build` directory may be hidden by default. To view it: **Package Explorer → ⋮ → Filters and Customization → uncheck "Gradle build folder"**. For Maven, the `target` directory is visible by default.

### Maven Wrapper (command line)

Run the following in a local command prompt:

On Linux or Mac:

```shell
./mvnw clean verify
```

On Windows:

```shell
mvnw.cmd clean verify
```

This creates a WAR file inside the `cics-java-liberty-springboot-jms-app/target` directory.

## Deploying to a CICS Liberty JVM server

### IBM MQ configuration

Before deploying, configure the following on your MQ queue manager:

- An MQ queue manager listening on an accessible TCP/IP port
- A queue named `SPRING.QUEUE`

> **Note:** Queues should be defined as shareable to allow usage in the multi-threaded Liberty environment. It is also advisable to set the `BackoutThreshold` attribute on the queue to prevent the MDP being constantly re-driven if message processing fails.

> **Note:** The queue name `SPRING.QUEUE` is defined as the static variable `MDP_QUEUE` in `JMSMessageReceiver.java`. If the name does not meet your enterprise naming standards, update that variable before building.

### CICS Liberty configuration

Ensure you have the following features defined in your Liberty `server.xml`:

- `servlet-6.0` (required for Spring Boot 3.x and Jakarta EE 10)
- `concurrent-3.0`
- `messaging-3.1`
- `wmqJmsClient-2.0`
- `jndi-1.0`

Also add the JMS MQ Connection Factory configuration to `server.xml`. Substitute the *channel*, *hostname*, *port*, and *queueManager* values for your installation, and set the MQ RAR location to the path on zFS where you downloaded the resource adapter:

```xml
<jmsConnectionFactory id="jms/cf" jndiName="jms/cf">
    <properties.wmqJms channel="yourChannel" hostname="yourHost" port="yourPort"
    queueManager="yourQueueManager" transportType="CLIENT"/>
    <connectionManager maxPoolSize="10" minPoolSize="0"/>
</jmsConnectionFactory>
<variable name="wmqJmsClient.rar.location" value="/wmq.jmsra-9.0.4.0.rar"/>
```

> **Note:** The value of `10` on `maxPoolSize` is an example only. Set it to the maximum number of concurrent users of the connection factory.

A template `server.xml` is provided [here](./etc/config/liberty/server.xml).

### CICS Bundle Plugin Deployment (Gradle/Maven)

This method uses the CICS bundle generated during the build process.

**Configure your JVM server name:**

Gradle (`cics-java-liberty-springboot-jms-cicsbundle/build.gradle`):
```gradle
cics.jvmserver = 'YOUR_JVMSERVER_NAME'  // e.g., 'DFHWLP'
```

Maven (`cics-java-liberty-springboot-jms-cicsbundle/pom.xml`):
```xml
<cics.jvmserver>YOUR_JVMSERVER_NAME</cics.jvmserver>  <!-- e.g., DFHWLP -->
```

**Deploy the bundle:**

1. Upload the CICS bundle ZIP file to zFS:
   - Gradle: `cics-java-liberty-springboot-jms-cicsbundle/build/distributions/cics-java-liberty-springboot-jms-cicsbundle-1.0.0.zip`
   - Maven: `cics-java-liberty-springboot-jms-cicsbundle/target/cics-java-liberty-springboot-jms-cicsbundle-1.0.0.zip`

2. Unzip the bundle on zFS

3. Create a CICS BUNDLE resource definition:
   ```
   CEDA DEFINE BUNDLE(JMSSAMP) GROUP(MYGROUP) BUNDLEDIR(/path/to/bundle)
   ```

4. Install the bundle:
   ```
   CEDA INSTALL BUNDLE(JMSSAMP) GROUP(MYGROUP)
   ```

---

### CICS Explorer SDK Deployment

This repository includes a pre-configured Eclipse CICS bundle project `cics-java-liberty-springboot-jms-cicsbundle-eclipse` that can be used directly with CICS Explorer SDK.

1. Right-click the `cics-java-liberty-springboot-jms-cicsbundle-eclipse` project → **Export Bundle Project to z/OS UNIX File System** and follow the wizard

> **Note**: The bundle project is pre-configured so that the Eclipse WTP export automatically packages the application WAR with all dependencies. This relies on the `-app` project being open in the same Eclipse workspace. If you have not yet imported the project, follow steps 3 and 6 of the [Importing into Eclipse](#downloading) instructions first.

---

### Direct Liberty Application Deployment

1. Manually upload the WAR file to zFS
2. Add an `<application>` element to the Liberty server.xml to define the web application with access to all authenticated users. For example:

```xml
<application id="cics-java-liberty-springboot-jms"
    location="${server.config.dir}/springapps/cics-java-liberty-springboot-jms.war"
    name="cics-java-liberty-springboot-jms" type="war">
    <application-bnd>
        <security-role name="cicsAllAuthenticated">
            <special-subject type="ALL_AUTHENTICATED_USERS"/>
        </security-role>
    </application-bnd>
</application>
```

---

## Running the Sample

1. Ensure the web application started successfully in Liberty by checking for msg `CWWKT0016I` in the Liberty messages.log:
   ```
   CWWKT0016I: Web application available (default_host): http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jms
   SRVE0292I: Servlet Message - [cics-java-liberty-springboot-jms]:.Initializing Spring embedded WebApplicationContext
   ```

2. Send a message to MQ by invoking the REST endpoint with your TSQ name and message data:
   ```
   http://myzos.mycompany.com:httpPort/cics-java-liberty-springboot-jms/send/SPRING.QUEUE?data=I LOVE CICS
   ```

3. Check if the specified TSQ has the information you expected by executing the CICS 3270 command `CEBR SPRINGQ`. You should see `I LOVE CICS` in TSQ SPRINGQ.

4. Check that the Spring MDP Bean was driven by viewing the messages in the Liberty messages.log.

---

## Troubleshooting

**Issue: Application fails to start**
- Check Liberty messages.log for errors
- Verify all required features are enabled in `server.xml` (see [CICS Liberty configuration](#cics-liberty-configuration))
- Confirm CICS TS version supports Spring Boot 3.x (V6.1+)

**Issue: Messages not appearing in TSQ**
- Verify the MQ queue manager is accessible and `SPRING.QUEUE` exists and is defined as shareable
- Check the MQ connection factory configuration in `server.xml` matches your installation
- Review Liberty messages.log for JMS connection errors

## License

This project is licensed under [Eclipse Public License - v 2.0](LICENSE).

## Additional Resources

- [CICS TS Documentation](https://www.ibm.com/docs/en/cics-ts)
- [Spring Boot Java applications for CICS, Part 5: JMS](https://developer.ibm.com/tutorials/spring-boot-java-applications-for-cics-part-5-jms/)
- [Spring Integration JMS Documentation](https://docs.spring.io/spring-integration/reference/jms.html)
- [IBM MQ Resource Adapter download](https://www.ibm.com/support/pages/node/489235)

## Contributing

This sample is maintained by IBM CICS development. We welcome bug reports and feature requests via GitHub Issues. Contributions are welcome and reviewed on a case-by-case basis — please read the [contributing guidelines](https://github.com/cicsdev/.github/blob/main/CONTRIBUTING.md) before opening a pull request. For CICS product questions, contact IBM Support.
