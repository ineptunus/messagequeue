package com.example.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ConsumerApp {
    private static final List<String> VALID_TYPES = List.of("email", "sms", "fcm");

    public static void main(String[] args) throws Exception {
        String consumerType = args.length > 0 ? args[0].toLowerCase() : "email";
        if (!VALID_TYPES.contains(consumerType)) {
            System.out.println("Usage: java -jar consumer.jar [email|sms|fcm]");
            System.exit(1);
        }

        String rabbitUrl = env("RABBITMQ_URL", "amqp://guest:guest@localhost:5672/");
        String exchangeName = env("EXCHANGE_NAME", "notifications");
        String queueName = "notifications." + consumerType;

        Connection connection = RabbitMqConnectionFactory.newConnection(rabbitUrl);
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, "fanout", true);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchangeName, "");

        ObjectMapper mapper = new ObjectMapper();
        System.out.printf("[%s consumer] Waiting for messages on queue '%s'%n", consumerType, queueName);

        DeliverCallback callback = (consumerTag, delivery) -> {
            String payload = new String(delivery.getBody(), StandardCharsets.UTF_8);
            NotificationMessage message = mapper.readValue(payload, NotificationMessage.class);
            handleMessage(consumerType, message);
        };

        channel.basicConsume(queueName, true, callback, consumerTag -> {
        });
        Thread.currentThread().join();
    }

    private static void handleMessage(String consumerType, NotificationMessage message) {
        String output = switch (consumerType) {
            case "email" -> String.format("[EMAIL] Send email to %s: %s", message.getUserId(), message.getContent());
            case "sms" -> String.format("[SMS] Send SMS to %s: %s", message.getUserId(), message.getContent());
            case "fcm" -> String.format("[FCM] Send push to %s: %s", message.getUserId(), message.getContent());
            default -> String.format("[UNKNOWN] Received message: %s", message.getContent());
        };
        System.out.println(output);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
