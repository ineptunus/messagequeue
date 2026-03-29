# Java Pub/Sub Sample

Sample Java implementation of a Pub/Sub messaging flow using RabbitMQ.
This project is inspired by the `notification_publisher` and `notification_consumer` repositories from GitHub.

## Overview

- `publisher` contains a simple HTTP publisher that sends JSON messages to a RabbitMQ fanout exchange.
- `consumer` contains a CLI-based subscriber that receives messages from a bound queue.

## Requirements

- Java 17+
- Maven 3.8+
- RabbitMQ running on `localhost:5672`

## Run with Docker RabbitMQ

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

## Build all modules

```bash
cd "d:/SEMESTER 6/topik khusus/message_queue"
mvn clean package
```

## Start publisher

```bash
cd "d:/SEMESTER 6/topik khusus/message_queue/publisher"
mvn exec:java -Dexec.mainClass="com.example.pubsub.PublisherHttpServer"
```

## Start consumers

```bash
cd "d:/SEMESTER 6/topik khusus/message_queue/consumer"
mvn exec:java -Dexec.mainClass="com.example.pubsub.ConsumerApp" -Dexec.args="email"
```

You can run the same command with `sms` or `fcm`.

## Publish a message

```bash
curl -X POST http://localhost:8080/publish \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-001","userId":"USR-123","content":"Hello from Java publisher","timestamp":"2026-03-29T10:00:00Z"}'
```

## Notes

- The publisher uses a fanout exchange named `notifications`, matching the Go sample architecture.
- Each consumer creates its own queue and binds to the same exchange.
- This sample is intentionally lightweight and uses the standard JDK HTTP server.
