package com.example.pubsub;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public final class RabbitMqConnectionFactory {
    private RabbitMqConnectionFactory() {
    }

    public static Connection newConnection(String uri) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(uri);
        return factory.newConnection();
    }
}
