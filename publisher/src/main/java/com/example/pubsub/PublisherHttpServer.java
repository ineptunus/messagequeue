package com.example.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PublisherHttpServer {
    public static void main(String[] args) throws Exception {
        String rabbitUrl = env("RABBITMQ_URL", "amqp://guest:guest@localhost:5672/");
        String exchangeName = env("EXCHANGE_NAME", "notifications");
        int port = Integer.parseInt(env("PORT", "8080"));

        Connection connection = RabbitMqConnectionFactory.newConnection(rabbitUrl);
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(exchangeName, "fanout", true);

        ObjectMapper mapper = new ObjectMapper();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/publish", exchange -> {
            try {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, Map.of("error", "Method not allowed"));
                    return;
                }

                NotificationMessage message = parseRequest(exchange.getRequestBody(), mapper);
                if (message == null || isEmpty(message.getOrderId()) || isEmpty(message.getUserId())
                        || isEmpty(message.getContent())) {
                    sendJson(exchange, 400, Map.of("error", "Missing required fields: orderId, userId, content"));
                    return;
                }

                byte[] payload = mapper.writeValueAsBytes(message);
                channel.basicPublish(exchangeName, "", null, payload);

                sendJson(exchange, 200, Map.of("message", "Published successfully"));
                System.out.printf("Published message orderId=%s userId=%s%n", message.getOrderId(),
                        message.getUserId());
            } catch (Exception cause) {
                cause.printStackTrace();
                sendJson(exchange, 500, Map.of("error", "Unable to publish message"));
            }
        });

        server.start();
        System.out.printf("Publisher running at http://localhost:%d/publish%n", port);
    }

    private static NotificationMessage parseRequest(InputStream body, ObjectMapper mapper) throws IOException {
        return mapper.readValue(body, NotificationMessage.class);
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Map<String, Object> data) throws IOException {
        byte[] payload = new ObjectMapper().writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}
