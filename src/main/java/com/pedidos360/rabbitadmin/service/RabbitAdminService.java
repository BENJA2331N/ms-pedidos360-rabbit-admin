package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.dto.BindingAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.ExchangeAdminRequestDTO;
import com.pedidos360.rabbitadmin.dto.QueueAdminRequestDTO;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void createQueue(QueueAdminRequestDTO dto) {
        Queue queue = new Queue(dto.getName().trim(), dto.isDurable());
        amqpAdmin.declareQueue(queue);
    }

    public boolean deleteQueue(String name) {
        return amqpAdmin.deleteQueue(name.trim());
    }

    public void createExchange(ExchangeAdminRequestDTO dto) {
        Exchange exchange = switch (dto.getType()) {
            case "topic" -> new TopicExchange(dto.getName().trim(), true, false);
            case "fanout" -> new FanoutExchange(dto.getName().trim(), true, false);
            default -> new DirectExchange(dto.getName().trim(), true, false);
        };
        amqpAdmin.declareExchange(exchange);
    }

    public boolean deleteExchange(String name) {
        return amqpAdmin.deleteExchange(name.trim());
    }

    public void createBinding(BindingAdminRequestDTO dto) {
        Binding binding = new Binding(
                dto.getQueueName().trim(),
                Binding.DestinationType.QUEUE,
                dto.getExchangeName().trim(),
                dto.getRoutingKey().trim(),
                null
        );
        amqpAdmin.declareBinding(binding);
    }
}
