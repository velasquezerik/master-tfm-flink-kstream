# xad-inventory-poc-kstream

## About

Proof Of Concept for the assessment of a new technology of the Event Streaming Processing Platform of the Inventory application.
This case covers the Kafka Streams approach.

## Technical Stack:

- Java 11+
- Maven 3.6+
- Spring Boot 2.7.5
    - Spring Boot Actuator for exposing management endopoints (health, info and prometheus)
- Spring Cloud 2021.0.5
    - Spring Cloud Sleuth for distributed tracing
    - Resilience4j for resilience patterns (circuit breaker, bulkhead and retries)
- Lombok abstraction (included in Spring Boot)
- Mapstruct for bean mapping (included in Spring Boot)
- Open API documentation (available at /swagger-ui.html)
- REST API model validation 
- JUnit 5, Cucumber and Spring Boot Test for testing purposes
- Logback for logging
    - Log patterns for local and cloud SPRING profiles includes Sleuth headers
- Apache Kafka Streams 3.1.2 for event stream processing
- AVRO for record serialization and deserialization
- Confluent kafka-streams-avro-serde for Specific (DTO/POJO) serialization and deserialization 
- AVRO maven plugin for DTO generation from AVRO Schema
- Elastic APM agent for exporting Micrometer metrics and APM transactions to Elastic
- Micrometer Prometheus for exposing metrics in Prometheus format



## Installation
This application is configured with two SPRING profiles:
- "local": For local dev environments.
- "cloud": For dockerized environments, where application properties are set by ENV variables.

Test on the browser via SWAGGER
-------------------

```sh
http://localhost:8080/kafka-stream-signal-excess/actuator
```
