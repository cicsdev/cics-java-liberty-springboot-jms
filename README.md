# cics-java-liberty-springboot-jms
[![Build](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml/badge.svg)](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml)
[![License](https://img.shields.io/badge/License-EPL%202.0-green.svg)](https://www.eclipse.org/legal/epl-2.0/)

This sample demonstrates how to use the Spring Boot JMS template to integrate with CICS and use IBM MQ as the message provider. The sample is intended for deployment to a CICS Liberty JVM server.

Invoking the REST endpoint of the application writes a message with the data you provide to MQ. A JMS listener endpoint receives the message from MQ and writes it to a CICS Temporary Storage Queue (TSQ). Reading from the JMS queue and writing to the CICS TSQ are performed within the same transaction using JTA via a Spring Boot container managed transaction, so everything commits or rolls back as one recoverable unit.

## Versions

Select the branch for your target environment:

| Spring Boot | Branch | Min Java | Jakarta EE | Build Status |
|-------------|--------|----------|------------|--------------|
| 4.x | [springboot/v4](../../tree/springboot/v4) | 21 | EE 11 | [![Build](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml/badge.svg?branch=springboot%2Fv4)](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml) |
| 3.x | [springboot/v3](../../tree/springboot/v3) | 17 | EE 10 | [![Build](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml/badge.svg?branch=springboot%2Fv3)](https://github.com/cicsdev/cics-java-liberty-springboot-jms/actions/workflows/build.yaml) |

All branches target CICS TS V6.1 or later.

## License
This project is licensed under [Eclipse Public License - v 2.0](LICENSE).

## Contributing

This sample is maintained by IBM CICS development. We welcome bug reports and feature requests via GitHub Issues. Contributions are welcome and reviewed on a case-by-case basis — please read the [contributing guidelines](https://github.com/cicsdev/.github/blob/main/CONTRIBUTING.md) before opening a pull request. For CICS product questions, contact IBM Support.
